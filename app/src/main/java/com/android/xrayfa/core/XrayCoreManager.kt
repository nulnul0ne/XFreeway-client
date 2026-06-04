package com.android.xrayfa.core

import android.content.Context
import android.net.TrafficStats
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringDef
import com.android.xrayfa.R
import com.android.xrayfa.common.di.qualifier.Application
import com.android.xrayfa.common.di.qualifier.Background
import com.android.xrayfa.common.repository.SettingsRepository
import com.android.xrayfa.parser.ParserFactory
import com.android.xrayfa.utils.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import java.util.function.Consumer
import javax.inject.Inject
import javax.inject.Singleton

const val TAG_PROXY = "proxy"
const val TAG_DIRECT = "direct"
@StringDef(value = [
    TAG_PROXY,
    TAG_DIRECT
])
@Retention(AnnotationRetention.SOURCE)
annotation class Tag

const val UP_STEAM = "uplink"
const val DOWN_STEAM = "downlink"
@StringDef(value =[
    UP_STEAM,
    DOWN_STEAM
])
annotation class Stream

@Singleton
class XrayCoreManager
@Inject constructor(
    @Application private val context: Context,
    @Background private val coroutineScope: CoroutineScope,
    private val parserFactory: ParserFactory,
    private val settingsRepository: SettingsRepository
): TrafficDetector {

    companion object {
        const val TAG = "XrayCoreManager"
    }
    private var coreController: CoreController? = null
    private var job: Job? = null
    private var consumeJob: Job? = null
    private var startOrClose = false
    private val trafficChannel = Channel<Pair<Double, Double>>(capacity = Channel.CONFLATED)
    private val consumes: MutableList<Consumer<Pair<Double, Double>>> = ArrayList()

    val controllerHandler = object: CoreCallbackHandler {
        override fun onEmitStatus(p0: Long, p1: String?): Long {
            Log.i(TAG, "onEmitStatus: $p0 $p1")
            if (startOrClose)
                startTrafficDetection()
            else
                stopTrafficDetection()
            return 0L
        }

        override fun shutdown(): Long {
            Log.i(TAG, "shutdown: end")
            if (consumeJob?.isActive == true) consumeJob?.cancel()
            return 0L
        }

        override fun startup(): Long {
            Log.i(TAG, "startup: start")
            consumeJob = coroutineScope.launch(Dispatchers.Default) {
                consumeTraffic()
            }
            return 0L
        }

    }
    init {

        Log.i(TAG, "${context.filesDir.absolutePath}")
        Libv2ray.initCoreEnv(
            context.filesDir.absolutePath, Device.getDeviceIdForXUDPBaseKey()
        )
        coroutineScope.launch {
            val xrayCoreVersion = Libv2ray.checkVersionX()
            if (settingsRepository.settingsFlow.first().xrayCoreVersion != xrayCoreVersion) {
                settingsRepository.setXrayCoreVersion(xrayCoreVersion)
            }
        }
        coreController = Libv2ray.newCoreController(controllerHandler)
    }


    fun measureDelaySync(url: String): Long {
        if (coreController?.isRunning == false) {
            return -1
        }
        var delay = 0L
        try {
            delay = coreController?.measureDelay(url) ?:0L
        }catch (e: Exception) {
            Log.e(TAG, "measureDelaySync: ${e.message}", )
            return -1
        }
        return delay
    }

    suspend fun startXrayCore(startOptions: StartOptions, tunFd: Int?): Boolean {
        try {
            tunFd?.let {
                coreController?.startLoop(parserFactory.getParser(startOptions.url).parse(startOptions),tunFd)
            }
            startOrClose = true
            startTrafficDetection()
            return true
        }catch (e: Exception) {
            Log.e(TAG, "startXrayCore failed: ${e.message}")
            withContext(Dispatchers.Main) {
                Toast.makeText(context,R.string.core_start_failed, Toast.LENGTH_SHORT).show()
            }

            return false
        }
    }

    fun stopXrayCore() {
        startOrClose = false
        stopTrafficDetection()
        coreController?.stopLoop()
    }

    override fun startTrafficDetection() {
            job?.cancel()
            job = coroutineScope.launch(Dispatchers.IO) {
                var last = 0L
                var lastUp = 0L
                var lastDown = 0L
                var lastTotalTx = 0L
                var lastTotalRx = 0L
                var upSpeed: Double
                var downSpeed: Double
                while (true) {
                    val cur = System.currentTimeMillis()
                    val xrayUp = queryStats(TAG_PROXY, UP_STEAM) + queryStats(TAG_DIRECT, UP_STEAM)
                    val xrayDown = queryStats(TAG_PROXY, DOWN_STEAM) + queryStats(TAG_DIRECT, DOWN_STEAM)
                    val totalTx = TrafficStats.getTotalTxBytes().takeIf { it >= 0L } ?: 0L
                    val totalRx = TrafficStats.getTotalRxBytes().takeIf { it >= 0L } ?: 0L
                    val deltaTimeSec = (cur - last) / 1000.0
                    if (last != 0L && deltaTimeSec > 0) {
                        val xrayUpDelta = (xrayUp - lastUp).coerceAtLeast(0L)
                        val xrayDownDelta = (xrayDown - lastDown).coerceAtLeast(0L)
                        val totalUpDelta = (totalTx - lastTotalTx).coerceAtLeast(0L)
                        val totalDownDelta = (totalRx - lastTotalRx).coerceAtLeast(0L)
                        val upDelta = if (xrayUpDelta > 0L) xrayUpDelta else totalUpDelta
                        val downDelta = if (xrayDownDelta > 0L) xrayDownDelta else totalDownDelta
                        upSpeed = (upDelta / deltaTimeSec) / 1024
                        downSpeed = (downDelta / deltaTimeSec) / 1024
                    } else {
                        upSpeed = 0.0
                        downSpeed = 0.0
                    }
                    trafficChannel.send(Pair(upSpeed, downSpeed))
                    last = cur
                    lastUp = xrayUp
                    lastDown = xrayDown
                    lastTotalTx = totalTx
                    lastTotalRx = totalRx
                    delay(1_000L)
            }
        }
    }

    override fun stopTrafficDetection() {
        job?.cancel()
        Log.d(TAG, "stopTrafficDetection: ${job?.isActive}")
    }

    override fun addConsumer(consume: Consumer<Pair<Double, Double>>) {
        consumes.add(consume)
    }

    /**
     * transfer the up/download data to ui layer
     */
    override suspend fun consumeTraffic() {

        for (pair in trafficChannel) {
            consumes.forEach {
                it.accept(pair)
            }
            delay(3000L)
        }
    }

    /**
     * @param tag direct proxy dns .etc..
     * @param stream uplink or downlink
     * @return traffic todo may be ?
     */
    private fun queryStats(@Tag tag: String, @Stream stream: String): Long {
        return coreController?.queryStats(tag, stream) ?: 0L
    }
}

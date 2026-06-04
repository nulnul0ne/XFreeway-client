package com.android.xrayfa.ui.component

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.window.core.layout.WindowWidthSizeClass
import com.android.xrayfa.R
import com.android.xrayfa.dto.Node
import com.android.xrayfa.ui.navigation.Home
import com.android.xrayfa.ui.navigation.Settings
import com.android.xrayfa.viewmodel.XrayViewmodel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.animation.AnimatedVisibilityScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    xrayViewmodel: XrayViewmodel,
    bottomPadding: Dp = 0.dp,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onSettingsClick: () -> Unit = {}
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isExpanded = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val isMedium = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Home.title), fontWeight = FontWeight.Bold) },
                actions = {
                    with(sharedTransitionScope) {
                        IconButton(
                            onClick = onSettingsClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.sharedElement(
                                    sharedTransitionScope.rememberSharedContentState(key = Settings.route),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = bottomPadding)
        ) {
            if (isExpanded || isMedium) {
                ExpandedHomeContent(xrayViewmodel)
            } else {
                CompactHomeContent(xrayViewmodel) { showError = it }
            }

            ExceptionMessage(
                shown = showError,
                msg = stringResource(R.string.config_not_ready),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun CompactHomeContent(
    xrayViewmodel: XrayViewmodel,
    onShowError: (Boolean) -> Unit
) {
    val selectedNode by xrayViewmodel.getSelectedNode().collectAsState(null)
    val nodes by xrayViewmodel.nodes.collectAsState()
    val isRunning by xrayViewmodel.isServiceRunning.collectAsState()
    val upSpeed by xrayViewmodel.upSpeed.collectAsState()
    val downSpeed by xrayViewmodel.downSpeed.collectAsState()
    val nodeDelays by xrayViewmodel.nodeDelays.collectAsState()
    val testingNodeId by xrayViewmodel.testingNodeId.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StatusCard(isRunning, upSpeed, downSpeed)

        Spacer(modifier = Modifier.height(32.dp))

        V2rayStarterLarge(xrayViewmodel) {
            if (selectedNode == null) {
                coroutineScope.launch {
                    onShowError(true)
                    delay(2000L)
                    onShowError(false)
                }
                false
            } else true
        }

        Text(
            text = stringResource(if (isRunning) R.string.connected else R.string.disconnect),
            style = MaterialTheme.typography.titleMedium,
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )

        ConnectionIpPill(
            ipAddress = if (isRunning) selectedNode?.let(::displayNodeIp) else null
        )

        Spacer(modifier = Modifier.height(10.dp))

        ConnectionNodesCard(
            nodes = nodes,
            nodeDelays = nodeDelays,
            testingNodeId = testingNodeId,
            onChoose = { xrayViewmodel.setSelectedNode(it) },
            onTest = { xrayViewmodel.measureNodeTcpDelay(it) }
        )
    }
}

@Composable
fun ExpandedHomeContent(
    xrayViewmodel: XrayViewmodel
) {
    val selectedNode by xrayViewmodel.getSelectedNode().collectAsState(null)
    val nodes by xrayViewmodel.nodes.collectAsState()
    val isRunning by xrayViewmodel.isServiceRunning.collectAsState()
    val upSpeed by xrayViewmodel.upSpeed.collectAsState()
    val downSpeed by xrayViewmodel.downSpeed.collectAsState()
    val nodeDelays by xrayViewmodel.nodeDelays.collectAsState()
    val testingNodeId by xrayViewmodel.testingNodeId.collectAsState()

    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            V2rayStarterLarge(xrayViewmodel) { selectedNode != null }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(if (isRunning) R.string.connected else R.string.disconnect),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            ConnectionIpPill(
                ipAddress = if (isRunning) selectedNode?.let(::displayNodeIp) else null
            )
            Spacer(modifier = Modifier.height(24.dp))
            StatusCard(isRunning, upSpeed, downSpeed)
        }

        Column(
            modifier = Modifier.weight(1.2f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.connection_detail),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            ConnectionNodesCard(
                nodes = nodes,
                nodeDelays = nodeDelays,
                testingNodeId = testingNodeId,
                onChoose = { xrayViewmodel.setSelectedNode(it) },
                onTest = { xrayViewmodel.measureNodeTcpDelay(it) }
            )
        }
    }
}

@Composable
private fun ConnectionIpPill(
    ipAddress: String?
) {
    val value = ipAddress?.takeIf { it.isNotBlank() } ?: "N/A"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (ipAddress.isNullOrBlank()) 0.26f else 0.58f),
                shape = RoundedCornerShape(999.dp)
            )
            .background(MaterialTheme.colorScheme.primary.copy(alpha = if (ipAddress.isNullOrBlank()) 0.05f else 0.1f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "IP: $value",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (ipAddress.isNullOrBlank()) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

private fun displayNodeIp(node: Node): String {
    val address = node.address.trim()
    return when (address.lowercase()) {
        "fi1.myfreeway.ru" -> "194.124.210.92"
        "us1.myfreeway.ru" -> "23.172.217.240"
        "ru1.myfreeway.ru" -> "94.198.54.219"
        else -> address
    }
}

@Composable
private fun ConnectionNodesCard(
    nodes: List<Node>,
    nodeDelays: Map<Int, Long>,
    testingNodeId: Int?,
    onChoose: (Int) -> Unit,
    onTest: (Node) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.connection_detail),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        if (nodes.isEmpty()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.select_configuration_notify), style = MaterialTheme.typography.bodyLarge)
                }
            }
            return
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            nodes.forEach { node ->
                NodeCard(
                    node = node,
                    onChoose = { onChoose(node.id) },
                    onTest = { onTest(node) },
                    delayMs = nodeDelays[node.id] ?: -1,
                    testing = testingNodeId == node.id,
                    selected = node.selected,
                    roundCorner = true,
                    enableTest = true,
                    countryEmoji = node.countryISO
                )
            }
        }
    }
}

@Composable
fun StatusCard(isRunning: Boolean, upSpeed: Double, downSpeed: Double) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpeedItem(
                label = stringResource(R.string.upload_data),
                speed = upSpeed,
                icon = Icons.Default.KeyboardArrowUp,
                color = MaterialTheme.colorScheme.primary
            )
            VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp))
            SpeedItem(
                label = stringResource(R.string.download_data),
                speed = downSpeed,
                icon = Icons.Default.KeyboardArrowDown,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun SpeedItem(label: String, speed: Double, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "${String.format("%.1f", speed)} KB/s",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun V2rayStarterLarge(
    xrayViewmodel: XrayViewmodel,
    onCheck: () -> Boolean
) {
    val isRunning by xrayViewmodel.isServiceRunning.collectAsState()
    val context = LocalContext.current

    val buttonBaseColor by animateColorAsState(
        targetValue = if (isRunning) Color(0xFF10213D) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
        animationSpec = tween(durationMillis = 500),
        label = "buttonBaseColor"
    )
    val pulseStartColor = Color(0xFF58C7FF)
    val pulseEndColor = Color(0xFF4D6DFF)
    val glowColor = Color(0xFF58B7FF)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            xrayViewmodel.startXrayService(context)
        }
    }
    val scale = remember { Animatable(1.0f) }
    val breathTransition = rememberInfiniteTransition(label = "starterBreath")
    val buttonBreathScale by breathTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRunning) 1.09f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starterButtonBreath"
    )
    val logoPulseScale by breathTransition.animateFloat(
        initialValue = if (isRunning) 0.96f else 1.0f,
        targetValue = if (isRunning) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starterLogoPulseScale"
    )
    val logoPulseAlpha by breathTransition.animateFloat(
        initialValue = if (isRunning) 0.76f else 0.42f,
        targetValue = if (isRunning) 1.0f else 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starterLogoPulseAlpha"
    )
    // Re-launch the effect whenever the 'running' variable changes
    LaunchedEffect(isRunning) {
        // Step 1: Animate the scale up to 1.1f quickly
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 150)
        )

        // Step 2: Bounce back to 1.0f with a spring effect
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(226.dp)
    ) {
        if (isRunning) {
            StarterPulseRing(glowColor = glowColor, delayMillis = 0, baseSize = 168.dp)
            StarterPulseRing(glowColor = glowColor, delayMillis = 520, baseSize = 168.dp)
            StarterPulseRing(glowColor = glowColor, delayMillis = 1040, baseSize = 168.dp)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val buttonRadius = (168.dp.toPx() / 2f) * scale.value * buttonBreathScale
            val haloRadius = buttonRadius + 18.dp.toPx()
            val innerRadius = 57.dp.toPx() * if (isRunning) buttonBreathScale else 1.0f
            val activeCoreBrush = Brush.radialGradient(
                colors = listOf(
                    pulseStartColor.copy(alpha = 0.58f),
                    glowColor.copy(alpha = 0.36f),
                    pulseEndColor.copy(alpha = 0.18f),
                    pulseEndColor.copy(alpha = 0.0f)
                ),
                center = center,
                radius = buttonRadius
            )
            val innerGlowBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF8DDAFF).copy(alpha = 0.48f),
                    pulseStartColor.copy(alpha = 0.26f),
                    pulseEndColor.copy(alpha = 0.0f)
                ),
                center = center,
                radius = innerRadius
            )
            val haloBrush = Brush.radialGradient(
                colors = listOf(
                    pulseStartColor.copy(alpha = 0.18f),
                    pulseEndColor.copy(alpha = 0.08f),
                    pulseEndColor.copy(alpha = 0.0f)
                ),
                center = center,
                radius = haloRadius
            )

            if (isRunning) {
                drawCircle(
                    brush = haloBrush,
                    radius = haloRadius,
                    center = center
                )
            }
            if (isRunning) {
                drawCircle(
                    color = buttonBaseColor,
                    radius = buttonRadius,
                    center = center
                )
                drawCircle(
                    brush = activeCoreBrush,
                    radius = buttonRadius,
                    center = center
                )
            } else {
                drawCircle(
                    color = buttonBaseColor,
                    radius = buttonRadius,
                    center = center
                )
            }
            drawCircle(
                brush = if (isRunning) {
                    innerGlowBrush
                } else {
                    Brush.linearGradient(listOf(Color.White, Color.White))
                },
                alpha = if (isRunning) 1.0f else 0.035f,
                radius = innerRadius,
                center = center
            )
        }

        IconButton(
            onClick = {
                if (!onCheck()) return@IconButton
                if (!isRunning) {
                    val prepare = VpnService.prepare(context)
                    if (prepare != null) launcher.launch(prepare)
                    else xrayViewmodel.startXrayService(context)
                } else {
                    xrayViewmodel.stopXrayService(context)
                }
            },
            modifier = Modifier.size(168.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_mini),
                    contentDescription = "Toggle Service",
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            alpha = logoPulseAlpha
                            scaleX = logoPulseScale
                            scaleY = logoPulseScale
                        }
                )
            }
        }
    }
}

@Composable
private fun StarterPulseRing(
    glowColor: Color,
    delayMillis: Int,
    baseSize: Dp
) {
    val ringTransition = rememberInfiniteTransition(label = "starterPulseRing$delayMillis")
    val ringScale by ringTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.56f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, delayMillis = delayMillis),
            repeatMode = RepeatMode.Restart
        ),
        label = "starterRingScale$delayMillis"
    )
    val ringAlpha by ringTransition.animateFloat(
        initialValue = 0.62f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1750, delayMillis = delayMillis),
            repeatMode = RepeatMode.Restart
        ),
        label = "starterRingAlpha$delayMillis"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val radius = (baseSize.toPx() / 2f) * ringScale
        val ringBrush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = ringAlpha * 0.24f),
                Color(0xFF2D78F8).copy(alpha = ringAlpha * 0.11f),
                glowColor.copy(alpha = 0.0f)
            ),
            center = center,
            radius = radius
        )
        drawCircle(
            brush = ringBrush,
            radius = radius,
            center = center
        )
        drawCircle(
            color = glowColor.copy(alpha = ringAlpha * 0.5f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.8.dp.toPx())
        )
    }
}

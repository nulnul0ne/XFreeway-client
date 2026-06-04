package com.android.xrayfa.ui.component

import android.content.Intent
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.android.xrayfa.R
import com.android.xrayfa.BuildConfig
import com.android.xrayfa.ui.navigation.Config
import com.android.xrayfa.ui.navigation.Detail
import com.android.xrayfa.ui.navigation.Edit
import com.android.xrayfa.ui.navigation.Home
import com.android.xrayfa.ui.navigation.NavigateDestination
import com.android.xrayfa.ui.navigation.Subscription
import com.android.xrayfa.viewmodel.XrayViewmodel
import com.android.xrayfa.ui.component.BugReportDialog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.style.TextAlign
import com.android.xrayfa.ui.navigation.ScanQR
import com.android.xrayfa.ui.theme.JuraNeon
import com.android.xrayfa.viewmodel.XrayViewmodel.Companion.SUB_ALL
import com.android.xrayfa.viewmodel.XrayViewmodel.Companion.SUB_MANUAL

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConfigScreen(
    xrayViewmodel: XrayViewmodel,
    bottomPadding: Dp = 0.dp,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigate: (NavigateDestination) -> Unit
) {
    val nodes by xrayViewmodel.nodes.collectAsState()
    val queryNodes by xrayViewmodel.queryNodes.collectAsState()
    val qrBitMap by xrayViewmodel.qrBitmap.collectAsState()
    val deleteDialog by xrayViewmodel.deleteDialog.collectAsState()
    val bugReportDialog by xrayViewmodel.bugReportDialog.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var displayedSubscriptionId by remember {
        mutableStateOf(readSubscriptionId(context))
    }
    var subscriptionExpiresAt by remember {
        mutableStateOf(readSubscriptionExpiresAt(context))
    }
    var importedSubscriptionId by remember {
        mutableStateOf(readImportedSubscriptionId(context))
    }
    var pendingOrderId by remember {
        mutableStateOf(readPendingAndroidOrderId(context))
    }
    var pendingOrderGuestId by remember {
        mutableStateOf(readPendingAndroidOrderGuestId(context))
    }
    var storeExpanded by remember { mutableStateOf(importedSubscriptionId == null) }
    var importExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState()
    )
    // Observe the overlap fraction to determine if the list is scrolled
    val isScrolled by remember {
        derivedStateOf { scrollBehavior.state.overlappedFraction > 0f }
    }

    // Animate the shadow elevation for a smooth transition
    val appBarElevation by animateDpAsState(
        targetValue = if (isScrolled) 4.dp else 0.dp,
        label = "TopBarShadowElevation"
    )
    
    // Function to locate and scroll to a specific item by ID
    suspend fun scrollToItemById(id: Int) {
        val index = nodes.indexOfFirst { it.id == id }
        if (index != -1) {
            // Animate scroll to the found index
            listState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(pendingOrderId, pendingOrderGuestId) {
        val orderId = pendingOrderId ?: return@LaunchedEffect
        val guestId = pendingOrderGuestId ?: return@LaunchedEffect

        repeat(40) {
            val status = runCatching {
                refreshAndroidOrderStatus(context, orderId, guestId)
            }.getOrNull()

            when {
                status?.isPaid == true && !status.subscriptionUrl.isNullOrBlank() -> {
                    xrayViewmodel.importMyFreeWaySubscription(
                        url = status.subscriptionUrl,
                        onSuccess = {
                            val subscriptionId = extractSubscriptionId(status.subscriptionUrl)
                            saveImportedSubscriptionId(context, subscriptionId, status.expiresAt)
                            clearPendingAndroidOrder(context)
                            displayedSubscriptionId = subscriptionId
                            subscriptionExpiresAt = status.expiresAt
                            importedSubscriptionId = subscriptionId
                            pendingOrderId = null
                            pendingOrderGuestId = null
                            Toast.makeText(
                                context,
                                R.string.android_payment_confirmed,
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                    return@LaunchedEffect
                }

                status?.isExpired == true -> {
                    clearPendingAndroidOrder(context)
                    pendingOrderId = null
                    pendingOrderGuestId = null
                    Toast.makeText(
                        context,
                        R.string.android_payment_expired,
                        Toast.LENGTH_LONG
                    ).show()
                    return@LaunchedEffect
                }
            }
            delay(3_000)
        }
    }

    LaunchedEffect(displayedSubscriptionId, importedSubscriptionId) {
        val subscriptionId = displayedSubscriptionId?.takeIf { it.isNotBlank() }
            ?: return@LaunchedEffect
        val resolved = runCatching {
            resolveSubscriptionStatus(context, subscriptionId)
        }.getOrNull()
        if (!resolved?.expiresAt.isNullOrBlank()) {
            saveSubscriptionExpiresAt(context, resolved?.expiresAt)
            subscriptionExpiresAt = resolved?.expiresAt
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()){
            Surface(
                shadowElevation = appBarElevation,
                color = MaterialTheme.colorScheme.surface, // Use surface instead of background
                modifier = Modifier.zIndex(1f)
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Text(stringResource(Config.title), fontWeight = FontWeight.Bold)
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent, // Transparent to show Surface color
                            scrolledContainerColor = Color.Transparent
                        ),
                        scrollBehavior = scrollBehavior,
                    )
                    
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    .columnVerticalScrollbar(listState,4.dp)
            ) {
                item {
                    SubscriptionIdentityCard(subscriptionId = displayedSubscriptionId)
                }
                item {
                    SubscriptionStatusBars(
                        active = nodes.isNotEmpty() || !displayedSubscriptionId.isNullOrBlank(),
                        expiresAt = subscriptionExpiresAt
                    )
                }
                item {
                    ServerStatusCard()
                }
                item {
                    StorePlaceholder(
                        expanded = storeExpanded,
                        onExpandedChange = {
                            storeExpanded = it
                            if (it) {
                                importExpanded = false
                            }
                        },
                        onBuyClick = { option ->
                            scope.launch {
                                try {
                                    val checkout = createAndroidCheckout(context, option)
                                    saveGuestId(context, checkout.guestId)
                                    savePendingAndroidOrder(context, checkout)
                                    pendingOrderId = checkout.orderId
                                    pendingOrderGuestId = checkout.guestId
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(checkout.paymentUrl))
                                    )
                                } catch (error: Exception) {
                                    Toast.makeText(
                                        context,
                                        error.message ?: context.getString(R.string.store_checkout_coming_soon),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    )
                }
                item {
                    ExistingSubscriptionImportCard(
                        onImport = { url, onDone ->
                            xrayViewmodel.importMyFreeWaySubscription(
                                url = url,
                                onSuccess = {
                                    val subscriptionId = extractSubscriptionId(url)
                                    scope.launch {
                                        val resolved = runCatching {
                                            resolveSubscriptionStatus(context, subscriptionId)
                                        }.getOrNull()
                                        saveImportedSubscriptionId(
                                            context,
                                            subscriptionId,
                                            resolved?.expiresAt
                                        )
                                        displayedSubscriptionId = subscriptionId
                                        subscriptionExpiresAt = resolved?.expiresAt
                                        importedSubscriptionId = subscriptionId
                                        Toast.makeText(
                                            context,
                                            R.string.existing_subscription_imported,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onDone()
                                    }
                                },
                                onError = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    onDone()
                                }
                            )
                        },
                        importedSubscriptionId = importedSubscriptionId,
                        expanded = importExpanded,
                        onExpandedChange = {
                            importExpanded = it
                            if (it) {
                                storeExpanded = false
                            }
                        },
                        onImportExpanded = {
                            if (it) {
                                storeExpanded = false
                            }
                        },
                        onDetachImported = {
                            xrayViewmodel.detachImportedMyFreeWaySubscription {
                                clearImportedSubscriptionId(context)
                                    displayedSubscriptionId = readSubscriptionId(context)
                                    subscriptionExpiresAt = readSubscriptionExpiresAt(context)
                                    importedSubscriptionId = null
                                    storeExpanded = true
                                    importExpanded = false
                                    Toast.makeText(
                                    context,
                                    R.string.existing_subscription_detached,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
            }
        }

        qrBitMap?.let {
            Dialog(onDismissRequest = { xrayViewmodel.dismissDialog() }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            bitmap = qrBitMap!!.asImageBitmap(),
                            contentDescription = "qrcode",
                            modifier = Modifier.size(250.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                xrayViewmodel.exportConfigToClipboard(context)
                                xrayViewmodel.dismissDialog()
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.clipboard_export)
                            )
                        }
                    }
                }
            }
        }
        if (deleteDialog) {
            DeleteDialog(
                onDismissRequest = {xrayViewmodel.hideDeleteDialog()},
            ) {
                xrayViewmodel.deleteNodeFromDialog()
            }
        }

        if (bugReportDialog) {
            BugReportDialog(
                onDismiss = { xrayViewmodel.hideBugReportDialog() },
                onSubmit = { data ->
                    xrayViewmodel.submitBugReport(context, data)
                }
            )
        }

    }

}


fun LazyListState.isAtBottom(callBack: (Boolean)-> Unit): Boolean{
    val layoutInfo = layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount

    if (visibleItems.isEmpty() || totalItems == 0) return false

    val contentHeight = layoutInfo.totalItemsCount.takeIf { it > 0 }?.let {
        layoutInfo.visibleItemsInfo.sumOf { it.size }
    } ?: 0
    val viewportHeight = layoutInfo.viewportEndOffset

    if (contentHeight <= viewportHeight) return false

    val lastVisible = visibleItems.last()
    val isAtBottom =  lastVisible.index == totalItems - 1 &&
            lastVisible.offset + lastVisible.size <= viewportHeight
    callBack(isAtBottom)
    return isAtBottom
}
/**
 * A highly optimized, flicker-free vertical scrollbar modifier for LazyColumn.
 */
fun Modifier.columnVerticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    color: Color = Color.Gray,
    rightPadding: Dp = 2.dp,
    minThumbHeight: Dp = 20.dp // Prevent the scrollbar from disappearing on huge lists
): Modifier = composed {
    // 1. Use Animatable for smooth alpha transitions without triggering layout recompositions
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(state.isScrollInProgress) {
        if (state.isScrollInProgress) {
            alpha.animateTo(1f, tween(durationMillis = 150))
        } else {
            alpha.animateTo(0f, tween(durationMillis = 500))
        }
    }

    drawWithContent {
        drawContent()

        val currentAlpha = alpha.value
        // Return early if fully transparent
        if (currentAlpha == 0f) return@drawWithContent

        val layoutInfo = state.layoutInfo
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        val totalItemsCount = layoutInfo.totalItemsCount

        if (totalItemsCount == 0 || visibleItemsInfo.isEmpty()) return@drawWithContent

        // 2. Core Fix: Calculate average item size to prevent jitter when visible items count changes
        val averageItemSize = visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
        val viewportHeight = size.height
        // Estimate the total height of all items combined
        val estimatedTotalSize = averageItemSize * totalItemsCount

        // No need for a scrollbar if all content fits the screen
        if (estimatedTotalSize <= viewportHeight) return@drawWithContent

        // 3. Calculate Thumb Height
        val heightProportion = (viewportHeight / estimatedTotalSize).coerceIn(0f, 1f)
        val minHeightPx = minThumbHeight.toPx()
        // Ensure the scrollbar doesn't become too small to see
        val thumbHeight = (viewportHeight * heightProportion).coerceAtLeast(minHeightPx)

        // 4. Calculate Scroll Progress (Fraction between 0.0 and 1.0)
        val firstItem = visibleItemsInfo.first()
        // offset is usually negative when scrolled down, so we invert it
        val firstItemOffset = -firstItem.offset.toFloat()
        val estimatedScrollOffset = (firstItem.index * averageItemSize) + firstItemOffset
        val maxEstimatedScrollOffset = (estimatedTotalSize - viewportHeight).coerceAtLeast(1f)

        val scrollProgress = (estimatedScrollOffset / maxEstimatedScrollOffset).coerceIn(0f, 1f)

        // 5. Calculate final Y coordinate
        val scrollbarOffsetY = scrollProgress * (viewportHeight - thumbHeight)

        // 6. Draw the rounded scrollbar thumb
        drawRoundRect(
            color = color,
            topLeft = Offset(
                x = size.width - width.toPx() - rightPadding.toPx(),
                y = scrollbarOffsetY
            ),
            size = Size(width.toPx(), thumbHeight),
            alpha = currentAlpha,
            cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
        )
    }
}

@Composable
private fun SubscriptionIdentityCard(
    subscriptionId: String?
) {
    CompactGlassCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.subscription_id_label),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7E8AA0)
            )
            Text(
                text = subscriptionId?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.subscription_id_empty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (subscriptionId.isNullOrBlank()) Color(0xFF566176) else Color(0xFFD8E2F0),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SubscriptionStatusBars(
    active: Boolean,
    expiresAt: String?
) {
    val dateLabel = remember(expiresAt) { formatSubscriptionExpiresAt(expiresAt) }
    val hasDate = !dateLabel.isNullOrBlank()
    val effectiveActive = active && hasDate

    CompactGlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = if (effectiveActive) Arrangement.spacedBy(14.dp) else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusGlassBar(
                text = stringResource(
                    if (effectiveActive) R.string.subscription_status_active else R.string.subscription_status_inactive
                ),
                fontFamily = JuraNeon,
                accent = if (effectiveActive) Color(0xFF77F2A6) else Color(0xFFFF8F8F),
                modifier = if (effectiveActive) Modifier.weight(1f) else Modifier
            )
            if (effectiveActive) {
                StatusGlassBar(
                    text = dateLabel,
                    fontFamily = JuraNeon,
                    accent = Color(0xFFFFD76A),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ServerStatusCard() {
    var online by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        online = checkMadridHealth()
    }

    CompactGlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.server_status_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB9C6D8)
            )
            StatusDot(
                active = online != null,
                color = when (online) {
                    true -> Color(0xFF22C55E)
                    false -> Color(0xFFFF2D32)
                    null -> Color(0xFF64748B)
                }
            )
        }
    }
}

@Composable
private fun CompactGlassCard(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF071323).copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun StatusDot(
    active: Boolean,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                color = if (active) color else color.copy(alpha = 0.16f),
                shape = CircleShape
            )
            .border(
                width = 4.dp,
                color = color.copy(alpha = if (active) 0.55f else 0.25f),
                shape = CircleShape
            )
    )
}

@Composable
private fun StatusGlassBar(
    text: String,
    fontFamily: FontFamily,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = Color(0xFF07111F),
        modifier = modifier.border(
            width = 1.dp,
            color = accent.copy(alpha = 0.72f),
            shape = MaterialTheme.shapes.large
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = fontFamily),
            fontWeight = FontWeight.Bold,
            color = accent,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StorePlaceholder(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onBuyClick: (StoreOption) -> Unit
) {
    val options = remember {
        listOf(
            StoreOption(StorePlan.Basic, StorePeriod.Month, "50 ₽", "basic_1m"),
            StoreOption(StorePlan.Basic, StorePeriod.Year, "399 ₽", "basic_12m"),
            StoreOption(StorePlan.Plus, StorePeriod.Month, "89 ₽", "plus_1m"),
            StoreOption(StorePlan.Plus, StorePeriod.Year, "699 ₽", "plus_12m")
        )
    }
    var selectedOption by remember { mutableStateOf(options.first()) }
    val metal = if (selectedOption.plan == StorePlan.Basic) Silver else Gold

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 4.dp,
        color = Color(0xFF101827)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = stringResource(R.string.store_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.store_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB9C6D8)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFFB9C6D8)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StoreOptionCard(
                                option = options[0],
                                selected = selectedOption == options[0],
                                modifier = Modifier.weight(1f)
                            ) { selectedOption = options[0] }
                            StoreOptionCard(
                                option = options[1],
                                selected = selectedOption == options[1],
                                modifier = Modifier.weight(1f)
                            ) { selectedOption = options[1] }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StoreOptionCard(
                                option = options[2],
                                selected = selectedOption == options[2],
                                modifier = Modifier.weight(1f)
                            ) { selectedOption = options[2] }
                            StoreOptionCard(
                                option = options[3],
                                selected = selectedOption == options[3],
                                modifier = Modifier.weight(1f)
                            ) { selectedOption = options[3] }
                        }
                    }

                    Button(
                        onClick = { onBuyClick(selectedOption) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = metal.copy(alpha = 0.92f),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "${stringResource(R.string.store_buy_button)} · ${selectedOption.price}",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JuraNeon),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreOptionCard(
    option: StoreOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = if (option.plan == StorePlan.Basic) Silver else Gold
    val planLabel = when (option.plan) {
        StorePlan.Basic -> stringResource(R.string.store_plan_basic)
        StorePlan.Plus -> stringResource(R.string.store_plan_plus)
    }
    val periodLabel = when (option.period) {
        StorePeriod.Month -> stringResource(R.string.store_period_month)
        StorePeriod.Year -> stringResource(R.string.store_period_year)
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (selected) accent.copy(alpha = 0.18f) else Color(0xFF07111F),
        modifier = modifier
            .height(74.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = accent.copy(alpha = if (selected) 0.95f else 0.42f),
                shape = MaterialTheme.shapes.large
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = planLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    text = option.price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFB9C6D8)
            )
        }
    }
}

@Composable
private fun ExistingSubscriptionImportCard(
    onImport: (String, () -> Unit) -> Unit,
    importedSubscriptionId: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onImportExpanded: (Boolean) -> Unit,
    onDetachImported: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!importedSubscriptionId.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = importedSubscriptionId,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF7E8AA0),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDetachImported) {
                        Text(
                            text = stringResource(R.string.existing_subscription_detach),
                            color = Color(0xFFFF8F8F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val next = !expanded
                            onExpandedChange(next)
                            onImportExpanded(next)
                            if (!next) {
                                error = null
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = expanded,
                        onCheckedChange = {
                            onExpandedChange(it)
                            onImportExpanded(it)
                            if (!it) {
                                error = null
                            }
                        }
                    )
                    Text(
                        text = stringResource(R.string.existing_subscription_question),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD8E2F0),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .weight(1f)
                    )
                }
            }

            AnimatedVisibility(visible = expanded && importedSubscriptionId.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = {
                            input = it
                            error = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.existing_subscription_input_label)) },
                        placeholder = { Text("https://plus.myfreeway.ru/sub/...") },
                        isError = error != null,
                        supportingText = {
                            Text(error ?: stringResource(R.string.existing_subscription_hint))
                        }
                    )
                    Button(
                        onClick = {
                            val normalized = normalizeMyFreeWaySubscriptionUrl(input)
                            if (normalized == null) {
                                error = "Only basic.myfreeway.ru/sub/... or plus.myfreeway.ru/sub/..."
                                return@Button
                            }
                            importing = true
                            onImport(normalized) {
                                importing = false
                            }
                        },
                        enabled = !importing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E),
                            contentColor = Color(0xFF06111D)
                        )
                    ) {
                        Text(
                            text = if (importing) {
                                stringResource(R.string.existing_subscription_checking)
                            } else {
                                stringResource(R.string.existing_subscription_check)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private data class StoreOption(
    val plan: StorePlan,
    val period: StorePeriod,
    val price: String,
    val planCode: String
)

private data class AndroidCheckout(
    val guestId: String,
    val orderId: Long,
    val paymentUrl: String,
    val dryRun: Boolean
)

private data class AndroidOrderStatus(
    val status: String,
    val subscriptionUrl: String?,
    val expiresAt: String?
) {
    val isPaid: Boolean
        get() = status == "paid" || status == "succeeded"

    val isExpired: Boolean
        get() = status == "expired" || status == "canceled"
}

private data class ResolvedSubscriptionStatus(
    val status: String,
    val expiresAt: String?
)

private suspend fun createAndroidCheckout(
    context: Context,
    option: StoreOption
): AndroidCheckout = withContext(Dispatchers.IO) {
    val prefs = context.getSharedPreferences("myfreeway_android_store", Context.MODE_PRIVATE)
    val installId = prefs.getString("install_id", null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString("install_id", it).apply()
    }
    val savedGuestId = prefs.getString("guest_id", null)

    val body = JSONObject()
        .put("plan_code", option.planCode)
        .put("install_id", installId)
    if (!savedGuestId.isNullOrBlank()) {
        body.put("guest_id", savedGuestId)
    }

    val response = postJson("${BuildConfig.ANDROID_API_BASE_URL}/api/android/orders/create", body)
    if (!response.optBoolean("ok", false)) {
        throw IllegalStateException(response.optString("error", "Не удалось создать заказ"))
    }

    val guestId = response.optString("guest_id")
    val paymentUrl = response.optString("payment_url")
    if (guestId.isBlank() || paymentUrl.isBlank()) {
        throw IllegalStateException("Сервер не вернул ссылку на оплату")
    }

    prefs.edit().putString("guest_id", guestId).apply()

    AndroidCheckout(
        guestId = guestId,
        orderId = response.optLong("order_id"),
        paymentUrl = paymentUrl,
        dryRun = response.optBoolean("dry_run", true)
    )
}

private suspend fun resolveSubscriptionStatus(
    context: Context,
    subscriptionId: String
): ResolvedSubscriptionStatus = withContext(Dispatchers.IO) {
    val body = JSONObject().put("short_token", subscriptionId)
    val response = postJson("${BuildConfig.ANDROID_API_BASE_URL}/api/android/subscription/resolve", body)
    if (!response.optBoolean("ok", false)) {
        throw IllegalStateException(response.optString("error", "Subscription resolve failed"))
    }
    val subscription = response.optJSONObject("subscription")
        ?: throw IllegalStateException("Subscription metadata is missing")

    ResolvedSubscriptionStatus(
        status = subscription.optString("status").lowercase(),
        expiresAt = subscription.optString("expires_at").ifBlank { null }
    )
}

private suspend fun refreshAndroidOrderStatus(
    context: Context,
    orderId: Long,
    guestId: String
): AndroidOrderStatus = withContext(Dispatchers.IO) {
    val body = JSONObject()
        .put("order_id", orderId)
        .put("guest_id", guestId)

    val response = postJson("${BuildConfig.ANDROID_API_BASE_URL}/api/android/orders/status", body)
    val status = response.optString("status").ifBlank {
        if (response.optBoolean("ok", false) && response.optString("short_url").isNotBlank()) {
            "paid"
        } else {
            "pending"
        }
    }

    AndroidOrderStatus(
        status = status.lowercase(),
        subscriptionUrl = response.optString("issued_sub_url")
            .ifBlank { response.optString("short_url") }
            .ifBlank { null },
        expiresAt = response.optString("expires_at")
            .ifBlank { response.optString("issued_expires_at") }
            .ifBlank { null }
    )
}

private fun postJson(endpoint: String, body: JSONObject): JSONObject {
    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 10_000
        readTimeout = 20_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=utf-8")
        setRequestProperty("Accept", "application/json")
    }

    return try {
        connection.outputStream.use { stream ->
            stream.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        val payload = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (payload.isBlank()) {
            throw IllegalStateException("Пустой ответ сервера")
        }
        JSONObject(payload)
    } finally {
        connection.disconnect()
    }
}

private fun getStorePrefs(context: Context) =
    context.getSharedPreferences("myfreeway_android_store", Context.MODE_PRIVATE)

private fun readSubscriptionId(context: Context): String? {
    return getStorePrefs(context).getString("subscription_id", null)
}

private fun saveSubscriptionId(context: Context, subscriptionId: String) {
    getStorePrefs(context).edit().putString("subscription_id", subscriptionId).apply()
}

private fun readSubscriptionExpiresAt(context: Context): String? {
    return getStorePrefs(context).getString("subscription_expires_at", null)
}

private fun saveSubscriptionExpiresAt(context: Context, expiresAt: String?) {
    getStorePrefs(context).edit().apply {
        if (expiresAt.isNullOrBlank()) {
            remove("subscription_expires_at")
        } else {
            putString("subscription_expires_at", expiresAt)
        }
    }.apply()
}

private fun readImportedSubscriptionId(context: Context): String? {
    return getStorePrefs(context).getString("imported_subscription_id", null)
}

private fun saveImportedSubscriptionId(
    context: Context,
    subscriptionId: String,
    expiresAt: String? = null
) {
    getStorePrefs(context).edit()
        .putString("subscription_id", subscriptionId)
        .putString("imported_subscription_id", subscriptionId)
        .apply {
            if (!expiresAt.isNullOrBlank()) {
                putString("subscription_expires_at", expiresAt)
            }
        }
        .apply()
}

private fun clearImportedSubscriptionId(context: Context) {
    val prefs = getStorePrefs(context)
    val imported = prefs.getString("imported_subscription_id", null)
    val current = prefs.getString("subscription_id", null)
    prefs.edit().apply {
        remove("imported_subscription_id")
        remove("subscription_expires_at")
        if (current == imported) {
            remove("subscription_id")
        }
    }.apply()
}

private fun saveGuestId(context: Context, guestId: String) {
    getStorePrefs(context).edit().putString("guest_id", guestId).apply()
}

private fun savePendingAndroidOrder(context: Context, checkout: AndroidCheckout) {
    getStorePrefs(context).edit()
        .putLong("pending_order_id", checkout.orderId)
        .putString("pending_order_guest_id", checkout.guestId)
        .apply()
}

private fun readPendingAndroidOrderId(context: Context): Long? {
    val value = getStorePrefs(context).getLong("pending_order_id", -1L)
    return value.takeIf { it > 0L }
}

private fun readPendingAndroidOrderGuestId(context: Context): String? {
    return getStorePrefs(context).getString("pending_order_guest_id", null)
}

private fun clearPendingAndroidOrder(context: Context) {
    getStorePrefs(context).edit()
        .remove("pending_order_id")
        .remove("pending_order_guest_id")
        .apply()
}

private fun extractSubscriptionId(url: String): String {
    val normalized = normalizeMyFreeWaySubscriptionUrl(url) ?: return ""
    val uri = Uri.parse(normalized)
    return uri.pathSegments.lastOrNull().orEmpty()
}

private fun formatSubscriptionExpiresAt(expiresAt: String?): String? {
    if (expiresAt.isNullOrBlank()) return null

    return runCatching {
        val date = OffsetDateTime.parse(expiresAt)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDate()
        "до ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
    }.getOrNull()
}

private fun normalizeMyFreeWaySubscriptionUrl(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null

    val candidate = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "https://$trimmed"
    }

    val uri = runCatching { Uri.parse(candidate) }.getOrNull() ?: return null
    val host = uri.host?.lowercase() ?: return null
    if (host !in setOf("basic.myfreeway.ru", "plus.myfreeway.ru")) {
        return null
    }
    if (uri.scheme !in setOf("http", "https")) {
        return null
    }
    val segments = uri.pathSegments
    if (segments.size < 2 || segments[0] != "sub" || segments[1].isBlank()) {
        return null
    }
    return "https://$host/sub/${segments[1]}"
}

private suspend fun checkMadridHealth(): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL("${BuildConfig.ANDROID_API_BASE_URL}/api/health").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            connection.responseCode in 200..299
        } finally {
            connection.disconnect()
        }
    }.getOrDefault(false)
}

private val Silver = Color(0xFFC8D2DE)
private val Gold = Color(0xFFFFD76A)

private enum class StorePlan {
    Basic,
    Plus
}

private enum class StorePeriod {
    Month,
    Year
}

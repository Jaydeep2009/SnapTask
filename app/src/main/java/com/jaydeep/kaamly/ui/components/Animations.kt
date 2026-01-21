package com.jaydeep.kaamly.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen transition animations for navigation
 */
object ScreenTransitions {
    /**
     * Slide in from right, slide out to left
     */
    fun slideInFromRight(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    fun slideOutToLeft(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    /**
     * Slide in from left, slide out to right
     */
    fun slideInFromLeft(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    fun slideOutToRight(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    /**
     * Fade in/out
     */
    fun fadeIn(): EnterTransition {
        return fadeIn(animationSpec = tween(300))
    }
    
    fun fadeOut(): ExitTransition {
        return fadeOut(animationSpec = tween(300))
    }
    
    /**
     * Scale and fade for dialogs
     */
    fun scaleIn(): EnterTransition {
        return scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    fun scaleOut(): ExitTransition {
        return scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300))
    }
}

/**
 * Button press animation modifier
 */
fun Modifier.pressAnimation(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {}
        )
}

/**
 * Shake animation for errors
 */
@Composable
fun ShakeAnimation(
    enabled: Boolean,
    onAnimationComplete: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    
    LaunchedEffect(enabled) {
        if (enabled) {
            val shakeKeyframes = keyframes<Float> {
                durationMillis = 400
                0f at 0
                -10f at 50
                10f at 100
                -10f at 150
                10f at 200
                -5f at 250
                5f at 300
                0f at 400
            }
            
            animate(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = shakeKeyframes
            ) { value, _ ->
                offsetX = value
            }
            
            onAnimationComplete()
        }
    }
    
    Box(modifier = Modifier.graphicsLayer { translationX = offsetX }) {
        content()
    }
}

/**
 * Bounce animation for success
 */
@Composable
fun BounceAnimation(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounce"
    )
    
    Box(modifier = Modifier.scale(scale)) {
        content()
    }
}

/**
 * Slide up animation for bottom sheets
 */
fun slideUpEnter(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

fun slideDownExit(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(300))
}

/**
 * List item animation
 */
@Composable
fun AnimatedListItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L) // Stagger animation
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + 
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
    ) {
        content()
    }
}

/**
 * Expandable content animation
 */
@Composable
fun ExpandableContent(
    expanded: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300)),
        exit = shrinkVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300))
    ) {
        content()
    }
}

/**
 * Crossfade animation for content switching
 */
@Composable
fun <T> AnimatedContent(
    targetState: T,
    content: @Composable (T) -> Unit
) {
    androidx.compose.animation.AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "content"
    ) { state ->
        content(state)
    }
}

/**
 * Shimmer loading animation
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    showShimmer: Boolean = true
): androidx.compose.ui.graphics.Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.6f),
            androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.2f),
            androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.6f),
        )
        
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )
        
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = shimmerColors,
            start = androidx.compose.ui.geometry.Offset.Zero,
            end = androidx.compose.ui.geometry.Offset(
                x = translateAnimation.value,
                y = translateAnimation.value
            )
        )
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                androidx.compose.ui.graphics.Color.Transparent,
                androidx.compose.ui.graphics.Color.Transparent
            )
        )
    }
}

/**
 * Pulsing animation for notifications
 */
@Composable
fun PulsingDot(
    color: androidx.compose.ui.graphics.Color,
    dotSize: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = Modifier
            .scale(scale)
            .graphicsLayer {
                this.alpha = 1f - (scale - 1f) * 2
            }
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(dotSize)
        ) {
            drawCircle(color = color)
        }
    }
}

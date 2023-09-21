package com.example.gizo.advance.designsystem.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gizo.advance.designsystem.theme.AppTheme
import com.example.gizo.advance.designsystem.theme.md_theme_light_surface
import kotlinx.coroutines.launch

@Composable
internal fun LoadingButton(
    modifier: Modifier = Modifier,
    contentDesc: String,
    baseLineColor: Color = MaterialTheme.colorScheme.inversePrimary,
    progressLineColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Specifies the float animation for slowly drawing out the lines on entering
    val startValue = if (LocalInspectionMode.current) 0F else 1F
    val floatAnimValues = (0 until NUM_OF_LINES).map { remember { Animatable(startValue) } }
    LaunchedEffect(floatAnimValues) {
        (0 until NUM_OF_LINES).map { index ->
            launch {
                floatAnimValues[index].animateTo(
                    targetValue = 0F,
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = FastOutSlowInEasing,
                        delayMillis = 40 * index
                    )
                )
            }
        }
    }

    // Specifies the rotation animation of the entire Canvas composable
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ROTATION_TIME, easing = LinearEasing)
        ), label = ""
    )

    // Specifies the color animation for the base-to-progress line color change
    val colorAnimValues = (0 until NUM_OF_LINES).map { index ->
        infiniteTransition.animateColor(
            initialValue = baseLineColor,
            targetValue = baseLineColor,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = ROTATION_TIME / 2
                    progressLineColor at ROTATION_TIME / NUM_OF_LINES / 2 with LinearEasing
                    baseLineColor at ROTATION_TIME / NUM_OF_LINES with LinearEasing
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(ROTATION_TIME / NUM_OF_LINES / 2 * index)
            ), label = ""
        )
    }

    // Draws out the LoadingWheel Canvas composable and sets the animations
    Canvas(
        modifier = modifier
            .size(48.dp)
            .padding(8.dp)
            .graphicsLayer { rotationZ = rotationAnim }
            .semantics { contentDescription = contentDesc }
    ) {
        repeat(NUM_OF_LINES) { index ->
            rotate(degrees = index * 30f) {
                drawLine(
                    color = colorAnimValues[index].value,
                    // Animates the initially drawn 1 pixel alpha from 0 to 1
                    alpha = if (floatAnimValues[index].value < 1f) 1f else 0f,
                    strokeWidth = 4F,
                    cap = StrokeCap.Round,
                    start = Offset(size.width / 2, size.height / 4),
                    end = Offset(size.width / 2, floatAnimValues[index].value * size.height / 4)
                )
            }
        }
    }
}

@Composable
internal fun OverlayLoadingButton(
    contentDesc: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(60.dp),
        shadowElevation = 8.dp,
        color = md_theme_light_surface.copy(alpha = 0.83f),
        modifier = modifier
            .size(60.dp),
    ) {
        LoadingButton(
            contentDesc = contentDesc,
        )
    }
}

@Preview
@Composable
internal fun LoadingWheelPreview() {
    AppTheme {
        Surface {
            LoadingButton(contentDesc = "LoadingWheel")
        }
    }
}

@Preview
@Composable
internal fun OverlayLoadingWheelPreview() {
    AppTheme {
        Surface {
            OverlayLoadingButton(contentDesc = "LoadingWheel")
        }
    }
}

private const val ROTATION_TIME = 12000
private const val NUM_OF_LINES = 12


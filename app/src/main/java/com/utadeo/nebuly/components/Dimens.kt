package com.utadeo.nebuly.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Categorías de tamaño de pantalla
enum class ScreenSize {
    SMALL,   // < 360dp de ancho
    MEDIUM,  // 360dp - 600dp
    LARGE,   // 600dp - 840dp
    XLARGE   // > 840dp
}

@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    return when {
        screenWidth < 360.dp -> ScreenSize.SMALL
        screenWidth < 600.dp -> ScreenSize.MEDIUM
        screenWidth < 840.dp -> ScreenSize.LARGE
        else -> ScreenSize.XLARGE
    }
}

@Composable
fun getScreenHeight(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp.dp
}

@Composable
fun getScreenWidth(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp
}


object AppDimens {

    @Composable
    fun paddingHorizontal(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 16.dp
            ScreenSize.MEDIUM -> 24.dp
            ScreenSize.LARGE -> 32.dp
            ScreenSize.XLARGE -> 40.dp
        }
    }

    @Composable
    fun paddingVertical(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 16.dp
            ScreenSize.MEDIUM -> 20.dp
            ScreenSize.LARGE -> 24.dp
            ScreenSize.XLARGE -> 32.dp
        }
    }

    @Composable
    fun topSpacing(): Dp {
        val screenHeight = getScreenHeight()
        return when {
            screenHeight < 600.dp -> 40.dp
            screenHeight < 700.dp -> 60.dp
            screenHeight < 800.dp -> 80.dp
            else -> 100.dp
        }
    }


    @Composable
    fun buttonHeight(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 50.dp
            ScreenSize.MEDIUM -> 60.dp
            ScreenSize.LARGE -> 65.dp
            ScreenSize.XLARGE -> 70.dp
        }
    }

    @Composable
    fun avatarSize(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 120.dp
            ScreenSize.MEDIUM -> 150.dp
            ScreenSize.LARGE -> 180.dp
            ScreenSize.XLARGE -> 200.dp
        }
    }

    @Composable
    fun avatarSizeStore(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 160.dp
            ScreenSize.MEDIUM -> 200.dp
            ScreenSize.LARGE -> 240.dp
            ScreenSize.XLARGE -> 280.dp
        }
    }

    @Composable
    fun avatarSizeDetail(): Dp {
        val screenHeight = getScreenHeight()
        return when {
            screenHeight < 600.dp -> 200.dp
            screenHeight < 700.dp -> 240.dp
            screenHeight < 800.dp -> 260.dp
            else -> 280.dp
        }
    }

    @Composable
    fun backButtonSize(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 55.dp
            else -> 65.dp
        }
    }

    @Composable
    fun spacingSmall(): Dp = when (getScreenSize()) {
        ScreenSize.SMALL -> 8.dp
        else -> 12.dp
    }

    @Composable
    fun spacingMedium(): Dp = when (getScreenSize()) {
        ScreenSize.SMALL -> 16.dp
        ScreenSize.MEDIUM -> 20.dp
        else -> 24.dp
    }

    @Composable
    fun spacingLarge(): Dp = when (getScreenSize()) {
        ScreenSize.SMALL -> 24.dp
        ScreenSize.MEDIUM -> 32.dp
        else -> 40.dp
    }

    @Composable
    fun spacingExtraLarge(): Dp {
        val screenHeight = getScreenHeight()
        return when {
            screenHeight < 600.dp -> 40.dp
            screenHeight < 700.dp -> 60.dp
            else -> 80.dp
        }
    }

    @Composable
    fun titleSize(): Float = when (getScreenSize()) {
        ScreenSize.SMALL -> 70f
        ScreenSize.MEDIUM -> 90f
        else -> 90f
    }

    @Composable
    fun buttonTextSize(): Float = when (getScreenSize()) {
        ScreenSize.SMALL -> 20f
        ScreenSize.MEDIUM -> 25f
        else -> 25f
    }

    @Composable
    fun needsScroll(): Boolean {
        return getScreenHeight() < 700.dp
    }

    @Composable
    fun minContentHeight(): Dp {
        return getScreenHeight() - 100.dp // Restamos algo de espacio para padding
    }
}

@Composable
fun Dp.toPx(): Float {
    val density = LocalDensity.current
    return with(density) { this@toPx.toPx() }
}
package com.example.loyalisttest.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation.NavBackStackEntry
import com.example.loyalisttest.navigation.NavigationRoutes

object Transitions {

    // Определяем порядок экранов для правильной анимации
    private val screenOrder = mapOf(
        NavigationRoutes.Home.route to 0,
        NavigationRoutes.Catalog.route to 1,
        NavigationRoutes.Settings.route to 2
    )

    // Функция определения направления анимации
    private fun getDirection(
        from: NavBackStackEntry?,
        to: NavBackStackEntry?
    ): TransitionDirection {
        if (from == null || to == null) return TransitionDirection.RIGHT_TO_LEFT

        val fromOrder = screenOrder[from.destination.route] ?: 0
        val toOrder = screenOrder[to.destination.route] ?: 0

        return when {
            fromOrder < toOrder -> TransitionDirection.RIGHT_TO_LEFT
            fromOrder > toOrder -> TransitionDirection.LEFT_TO_RIGHT
            else -> TransitionDirection.RIGHT_TO_LEFT
        }
    }

    // Анимация входа с учетом направления
    fun bottomNavEnterTransition(
        from: NavBackStackEntry?,
        to: NavBackStackEntry?
    ): EnterTransition {
        return when(getDirection(from, to)) {
            TransitionDirection.RIGHT_TO_LEFT -> slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(animationSpec = tween(300))

            TransitionDirection.LEFT_TO_RIGHT -> slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { fullWidth -> -fullWidth }
            ) + fadeIn(animationSpec = tween(300))
        }
    }

    // Анимация выхода с учетом направления
    fun bottomNavExitTransition(
        from: NavBackStackEntry?,
        to: NavBackStackEntry?
    ): ExitTransition {
        return when(getDirection(from, to)) {
            TransitionDirection.RIGHT_TO_LEFT -> slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { fullWidth -> -fullWidth }
            ) + fadeOut(animationSpec = tween(300))

            TransitionDirection.LEFT_TO_RIGHT -> slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { fullWidth -> fullWidth }
            ) + fadeOut(animationSpec = tween(300))
        }
    }

    // Анимация для входа в авторизацию
    fun authEnterTransition(): EnterTransition {
        return fadeIn(animationSpec = tween(300)) +
                slideInHorizontally(
                    animationSpec = tween(500),
                    initialOffsetX = { fullWidth -> fullWidth / 2 }
                )
    }

    // Анимация для выхода из авторизации
    fun authExitTransition(): ExitTransition {
        return fadeOut(animationSpec = tween(300)) +
                slideOutHorizontally(
                    animationSpec = tween(500),
                    targetOffsetX = { fullWidth -> -fullWidth / 2 }
                )
    }

    // Анимация для модального QR-кода
    fun enterScale(): EnterTransition {
        return scaleIn(
            animationSpec = tween(300),
            initialScale = 0.8f
        ) + fadeIn(animationSpec = tween(300))
    }

    fun exitScale(): ExitTransition {
        return scaleOut(
            animationSpec = tween(300),
            targetScale = 0.8f
        ) + fadeOut(animationSpec = tween(300))
    }

    private enum class TransitionDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }
}
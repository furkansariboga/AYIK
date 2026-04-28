/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddEntry : Screen("add_entry?habitId={habitId}") {
        fun createRoute(habitId: Int? = null): String {
            return if (habitId != null) "add_entry?habitId=$habitId" else "add_entry"
        }
    }
    object Settings : Screen("settings")
    object SetupLock : Screen("setup_lock")
}

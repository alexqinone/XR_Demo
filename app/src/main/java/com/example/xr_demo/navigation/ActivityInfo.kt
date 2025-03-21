package com.example.xr_demo.navigation

data class ActivityInfo(
    val activityClass: Class<*>,
    val title: String,
    val description: String,
    val isFullSpace: Boolean = false
)
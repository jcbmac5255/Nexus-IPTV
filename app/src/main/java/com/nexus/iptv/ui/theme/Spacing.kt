package com.nexus.iptv.ui.theme

import com.nexus.iptv.ui.design.AppSpacing
import com.nexus.iptv.ui.design.LocalAppSpacing

typealias Spacing = AppSpacing

val LocalSpacing = LocalAppSpacing

fun defaultSpacing(): Spacing = AppSpacing()

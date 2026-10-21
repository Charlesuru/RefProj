package com.studiodraug.refproj.feature.debug

import android.os.Build

internal actual fun deviceDescription(): String =
    "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) · ${Build.MANUFACTURER} ${Build.MODEL}"

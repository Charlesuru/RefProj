package com.studiodraug.refproj.feature.debug

import platform.UIKit.UIDevice

internal actual fun deviceDescription(): String {
    val device = UIDevice.currentDevice
    return "${device.systemName} ${device.systemVersion} · ${device.model}"
}

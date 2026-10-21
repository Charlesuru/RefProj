package com.studiodraug.refproj.core.testing

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The newest API level the pinned Robolectric emulates; the app compiles against a newer SDK. */
const val ROBOLECTRIC_SDK = 36

// Both annotations are @Inherited, so every subclass in commonTest runs under Robolectric.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
actual abstract class PlatformTest actual constructor()

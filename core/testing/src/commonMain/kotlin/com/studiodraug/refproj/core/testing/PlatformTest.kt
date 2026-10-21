package com.studiodraug.refproj.core.testing

/**
 * Base class for `commonTest` classes that need the platform runtime: an Android `Context` (Room,
 * Android resources) or the Compose UI test environment. On the Android host such tests run under
 * Robolectric, a simulated Android SDK on the JVM; on iOS they run on the simulator as they are.
 *
 * Plain logic tests do not extend it: Robolectric adds a few seconds of start-up per test class.
 */
expect abstract class PlatformTest()

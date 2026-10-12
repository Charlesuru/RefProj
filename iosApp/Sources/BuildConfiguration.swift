import Foundation

/// Reads the build-time configuration that the active xcconfig wrote into Info.plist.
/// Fails fast: a missing value means a broken build configuration, never a runtime condition.
struct BuildConfiguration: Sendable {
    let environment: String
    let apiBaseUrl: String
    let versionName: String
    let buildNumber: String
    let isDebugBuild: Bool

    static let current = BuildConfiguration(bundle: .main)

    init(bundle: Bundle) {
        environment = Self.value("APP_ENVIRONMENT", in: bundle)
        apiBaseUrl = Self.value("API_BASE_URL", in: bundle)
        versionName = Self.value("CFBundleShortVersionString", in: bundle)
        buildNumber = Self.value("CFBundleVersion", in: bundle)
        #if DEBUG
        isDebugBuild = true
        #else
        isDebugBuild = false
        #endif
    }

    private static func value(_ key: String, in bundle: Bundle) -> String {
        guard let value = bundle.object(forInfoDictionaryKey: key) as? String, !value.isEmpty else {
            fatalError("Missing Info.plist key '\(key)'. Check iosApp/Configs/*.xcconfig.")
        }
        return value
    }
}

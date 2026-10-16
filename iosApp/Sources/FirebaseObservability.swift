import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
import Foundation
import Shared

/// Native Firebase behind the shared observability interfaces (`core:observability`).
///
/// Firebase is configured only when the build contains `GoogleService-Info.plist` (copied from
/// `iosApp/Firebase/<Env>/` by a build phase; never committed). Collection is off by default
/// (Info.plist). Crashlytics is switched on here for release builds only; Analytics is switched on
/// by shared code only with the user's consent (`ConsentRepositoryImpl`, `RestrictedAnalyticsTracker`).
enum FirebaseObservability {
    struct Bridges {
        let crashReporter: NativeCrashReporter?
        let analyticsTracker: AnalyticsTracker?
    }

    static func start(config: BuildConfiguration) -> Bridges {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else {
            return Bridges(crashReporter: nil, analyticsTracker: nil)
        }
        FirebaseApp.configure()
        let collect = !config.isDebugBuild
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(collect)
        Crashlytics.crashlytics().setCustomValue(config.environment, forKey: "environment")
        return Bridges(crashReporter: CrashlyticsReporter(), analyticsTracker: FirebaseAnalyticsTracker())
    }
}

/// Receives Kotlin exceptions as name, reason and Kotlin stack frames (`BridgedCrashReporter`).
final class CrashlyticsReporter: NSObject, NativeCrashReporter {
    func log(message: String) {
        Crashlytics.crashlytics().log(message)
    }

    func recordError(name: String, reason: String, stackTrace: [String]) {
        let model = ExceptionModel(name: name, reason: reason)
        model.stackTrace = stackTrace.map { StackFrame(symbol: $0, file: "", line: 0) }
        Crashlytics.crashlytics().record(exceptionModel: model)
    }

    func setUserId(id: String?) {
        Crashlytics.crashlytics().setUserID(id ?? "")
    }

    func setCustomKey(key: String, value: String) {
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }
}

final class FirebaseAnalyticsTracker: NSObject, AnalyticsTracker {
    func logEvent(event: AnalyticsEvent) {
        Analytics.logEvent(event.name, parameters: event.parameters)
    }

    func logScreenView(screenName: String) {
        Analytics.logEvent(AnalyticsEventScreenView, parameters: [AnalyticsParameterScreenName: screenName])
    }

    func setUserId(id: String?) {
        Analytics.setUserID(id)
    }

    func setCollectionEnabled(enabled: Bool) {
        Analytics.setAnalyticsCollectionEnabled(enabled)
    }
}

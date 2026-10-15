import Shared
import SwiftUI

@main
struct RefProjApp: App {
    init() {
        let config = BuildConfiguration.current
        let firebase = FirebaseObservability.start(config: config)
        let appConfig = AppConfig(
            environment: AppEnvironment.companion.parse(value: config.environment),
            apiBaseUrl: config.apiBaseUrl,
            versionName: config.versionName,
            buildNumber: config.buildNumber,
            isDebugBuild: config.isDebugBuild
        )
        MainViewControllerKt.startIosApp(
            config: appConfig,
            crashReporter: firebase.crashReporter,
            analyticsTracker: firebase.analyticsTracker
        )
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
        }
    }
}

/// Hosts the shared Compose Multiplatform UI.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

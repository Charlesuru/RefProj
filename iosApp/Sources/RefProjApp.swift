import Shared
import SwiftUI

@main
struct RefProjApp: App {
    init() {
        let config = BuildConfiguration.current
        MainViewControllerKt.startIosApp(
            environment: config.environment,
            apiBaseUrl: config.apiBaseUrl,
            versionName: config.versionName,
            buildNumber: config.buildNumber,
            isDebugBuild: config.isDebugBuild
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

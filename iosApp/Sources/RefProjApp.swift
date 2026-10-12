import Shared
import SwiftUI

@main
struct RefProjApp: App {
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
        let config = BuildConfiguration.current
        return MainViewControllerKt.MainViewController(
            environment: config.environment,
            apiBaseUrl: config.apiBaseUrl,
            versionName: config.versionName,
            buildNumber: config.buildNumber,
            isDebugBuild: config.isDebugBuild
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

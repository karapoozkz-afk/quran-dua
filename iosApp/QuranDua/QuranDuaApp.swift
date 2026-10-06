import SwiftUI
import Shared

@main
struct QuranDuaApp: App {
    @StateObject private var model = AppModel()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(model)
                .environment(\.layoutDirection, model.uiLanguage == "ar" ? .rightToLeft : .leftToRight)
                .task { await model.prepare() }
        }
    }
}

/// Holds the Kotlin SDK and the user's language; screens read from here.
@MainActor
final class AppModel: ObservableObject {
    let sdk = QuranDuaSdk(bundle: .main)
    @Published var uiLanguage: String
    @Published var favorites: Set<String> = []
    @Published var quranReady = false

    init() {
        let device = Locale.current.language.languageCode?.identifier ?? "en"
        let supported = RepositoriesKt.supportedUiLanguage(deviceLanguage: device)
        uiLanguage = supported.isEmpty ? "en" : supported
    }

    var strings: Strings { sdk.strings(uiLanguage: uiLanguage) }
    var lang: String { sdk.contentLanguage(uiLanguage: uiLanguage) }

    func text(_ localized: [String: String]?) -> String { sdk.text(localized: localized, lang: lang) }

    func prepare() async {
        favorites = (try? await sdk.favoriteIds()) ?? []
        _ = try? await sdk.prepareQuran()
        quranReady = true
    }

    func toggleFavorite(_ id: String) async {
        _ = try? await sdk.toggleFavorite(itemId: id)
        favorites = (try? await sdk.favoriteIds()) ?? favorites
    }
}

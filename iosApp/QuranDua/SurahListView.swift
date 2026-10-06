import SwiftUI
import Shared

struct SurahListView: View {
    @EnvironmentObject var model: AppModel
    @State private var surahs: [SurahInfo] = []

    var body: some View {
        Group {
            if surahs.isEmpty {
                ProgressView(model.strings.loadingQuran)
            } else {
                List(surahs, id: \.number) { surah in
                    NavigationLink {
                        SurahReaderView(surah: surah)
                    } label: {
                        HStack {
                            Text("\(surah.number). \(surah.transliteration)")
                            Spacer()
                            Text(surah.nameArabic)
                        }
                    }
                }
            }
        }
        .navigationTitle(model.strings.navQuran)
        .task(id: model.quranReady) {
            guard model.quranReady else { return }
            surahs = (try? await model.sdk.surahs(lang: model.lang)) ?? []
        }
    }
}

struct SurahReaderView: View {
    @EnvironmentObject var model: AppModel
    let surah: SurahInfo
    @State private var ayahs: [Ayah] = []

    var body: some View {
        List(ayahs, id: \.number) { ayah in
            VStack(alignment: .leading, spacing: 8) {
                Text("\(ayah.surah):\(ayah.number)").font(.caption).foregroundStyle(.secondary)
                Text(ayah.arabic)
                    .font(.custom("Amiri Quran", size: 26))
                    .frame(maxWidth: .infinity, alignment: .trailing)
                    .multilineTextAlignment(.trailing)
                Text(ayah.transliteration).italic()
                if let translation = ayah.translation { Text(translation) }
            }
        }
        .navigationTitle(surah.transliteration)
        .task { ayahs = (try? await model.sdk.ayahs(surah: surah.number, lang: model.lang)) ?? [] }
    }
}

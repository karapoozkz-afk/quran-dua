import SwiftUI
import Shared

struct RootView: View {
    @EnvironmentObject var model: AppModel

    var body: some View {
        TabView {
            NavigationStack { HomeView() }
                .tabItem { Label(model.strings.navHome, systemImage: "house") }
            NavigationStack { SurahListView() }
                .tabItem { Label(model.strings.navQuran, systemImage: "book") }
            NavigationStack { SearchView() }
                .tabItem { Label(model.strings.whatToRead, systemImage: "magnifyingglass") }
        }
    }
}

struct HomeView: View {
    @EnvironmentObject var model: AppModel

    var body: some View {
        List(model.sdk.situations, id: \.id) { situation in
            NavigationLink {
                SituationView(situation: situation)
            } label: {
                HStack(spacing: 14) {
                    Text(situation.emoji).font(.title2)
                    VStack(alignment: .leading) {
                        Text(model.text(situation.title)).font(.headline)
                        Text(model.text(situation.subtitle)).font(.caption).foregroundStyle(.secondary)
                    }
                }
            }
        }
        .navigationTitle(model.strings.situations)
    }
}

struct SituationView: View {
    @EnvironmentObject var model: AppModel
    let situation: Situation

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                if let notice = situation.notice {
                    Text(model.text(notice))
                        .padding()
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(.yellow.opacity(0.15), in: RoundedRectangle(cornerRadius: 12))
                }
                ForEach(model.sdk.duasFor(situationId: situation.id), id: \.id) { item in
                    DuaCardView(item: item)
                }
            }
            .padding()
        }
        .navigationTitle(model.text(situation.title))
    }
}

struct SearchView: View {
    @EnvironmentObject var model: AppModel
    @State private var query = ""

    var body: some View {
        let results = query.isEmpty ? nil : model.sdk.search(query: query)
        List {
            if let results {
                Section(model.strings.resultsSituations) {
                    ForEach(results.situations, id: \.id) { s in
                        NavigationLink(model.text(s.title)) { SituationView(situation: s) }
                    }
                }
                Section(model.strings.resultsDuas) {
                    ForEach(results.duas, id: \.id) { DuaCardView(item: $0) }
                }
            }
        }
        .searchable(text: $query, prompt: model.strings.searchHint)
        .navigationTitle(model.strings.whatToRead)
    }
}

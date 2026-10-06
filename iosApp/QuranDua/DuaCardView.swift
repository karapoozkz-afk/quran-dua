import SwiftUI
import Shared

/// Mirrors the Android DuaCard: the grade and the source are always shown.
struct DuaCardView: View {
    @EnvironmentObject var model: AppModel
    let item: DuaItem

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text(model.text(item.title)).font(.headline)
                Spacer()
                Button {
                    Task { await model.toggleFavorite(item.id) }
                } label: {
                    Image(systemName: model.favorites.contains(item.id) ? "heart.fill" : "heart")
                }
            }
            Text(model.strings.gradeLabel(item.grade))
                .font(.caption)
                .padding(.horizontal, 10).padding(.vertical, 4)
                .background(.green.opacity(0.15), in: Capsule())
            if let arabic = item.arabic, !arabic.isEmpty {
                Text(arabic)
                    .font(.custom("Amiri Quran", size: 26))
                    .multilineTextAlignment(.trailing)
                    .frame(maxWidth: .infinity, alignment: .trailing)
                    .environment(\.layoutDirection, .rightToLeft)
            }
            if let translit = item.translit {
                Text(model.text(translit)).italic()
            }
            Text(model.text(item.translation))
            if let note = item.note {
                Text(model.text(note)).font(.footnote).foregroundStyle(.secondary)
            }
            Divider()
            Text("\(model.strings.source): \(model.text(item.source))")
                .font(.footnote).foregroundStyle(.secondary)
        }
        .padding()
        .background(.background.secondary, in: RoundedRectangle(cornerRadius: 16))
    }
}

package app.qurandua.android.ui.duas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.components.DuaCard
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.DuaItem

@Composable
fun DuasScreen(
    duas: List<DuaItem>,
    favorites: Set<String>,
    onOpenInQuran: (AyahKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(duas, key = { it.id }) { dua ->
            DuaCard(item = dua, isFavorite = dua.id in favorites, onOpenInQuran = onOpenInQuran)
        }
    }
}

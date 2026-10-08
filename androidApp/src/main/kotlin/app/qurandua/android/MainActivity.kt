package app.qurandua.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.qurandua.android.ui.AppRoot
import app.qurandua.android.ui.AppViewModel
import app.qurandua.shared.data.supportedUiLanguage
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var permissionCallback: ((Boolean) -> Unit)? = null
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionCallback?.invoke(granted)
        permissionCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val deps = (application as QuranDuaApp).deps
        deps.permissions.launcher = { permission, onResult ->
            val alreadyGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            // POST_NOTIFICATIONS exists only on Android 13+; older versions allow notifications by default.
            val notNeeded = permission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            if (alreadyGranted || notNeeded) {
                onResult(true)
            } else {
                permissionCallback = onResult
                permissionLauncher.launch(permission)
            }
        }
        val deviceLanguage = supportedUiLanguage(Locale.getDefault().language).ifEmpty { "en" }

        setContent {
            val model: AppViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(
                        content = deps.content,
                        quran = deps.quran,
                        user = deps.user,
                        readQuranAsset = deps.readQuranAsset,
                        uiLanguageFallback = deviceLanguage,
                    ) as T
                },
            )
            AppRoot(viewModel = model, deps = deps)
        }
    }

    override fun onResume() {
        super.onResume()
        // Prayer times follow the phone: a trip from Almaty to Bishkek moves them on the next open.
        val deps = (application as QuranDuaApp).deps
        lifecycleScope.launch { deps.prayer.refreshAutoLocation() }
    }

    override fun onDestroy() {
        // The launcher belongs to this activity; drop it so the app-wide deps don't leak it.
        val deps = (application as QuranDuaApp).deps
        if (isFinishing) deps.permissions.launcher = null
        super.onDestroy()
    }
}

package app.bibliya.android

import android.Manifest
import android.content.Intent
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
import androidx.lifecycle.viewmodel.compose.viewModel
import app.bibliya.android.platform.EXTRA_OPEN_VERSE
import app.bibliya.android.ui.AppRoot
import app.bibliya.android.ui.AppViewModel
import app.bibliya.shared.data.supportedUiLanguage
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var permissionCallback: ((Boolean) -> Unit)? = null
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionCallback?.invoke(granted)
        permissionCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 15+ draws every app edge to edge; the screens pad themselves with Scaffold insets.
        enableEdgeToEdge()
        val deps = (application as BibleApp).deps
        deps.permissions.isGranted = { permission -> isGranted(permission) }
        deps.permissions.launcher = { permission, onResult ->
            if (isGranted(permission)) {
                onResult(true)
            } else {
                permissionCallback = onResult
                permissionLauncher.launch(permission)
            }
        }
        if (intent?.getBooleanExtra(EXTRA_OPEN_VERSE, false) == true) deps.openVerseRequested.value = true
        val deviceLanguage = supportedUiLanguage(Locale.getDefault().language).ifEmpty { "en" }

        setContent {
            val model: AppViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(
                        content = deps.content,
                        bible = deps.bible,
                        user = deps.user,
                        readBibleAsset = deps.readBibleAsset,
                        uiLanguageFallback = deviceLanguage,
                    ) as T
                },
            )
            AppRoot(viewModel = model, deps = deps)
        }
    }

    private fun isGranted(permission: String): Boolean {
        // POST_NOTIFICATIONS exists only on Android 13+; older versions allow notifications by default.
        if (permission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OPEN_VERSE, false)) (application as BibleApp).deps.openVerseRequested.value = true
    }

    override fun onDestroy() {
        // The launcher belongs to this activity; drop it so the app-wide deps don't leak it.
        val deps = (application as BibleApp).deps
        if (isFinishing) {
            deps.permissions.launcher = null
            deps.permissions.isGranted = null
        }
        super.onDestroy()
    }
}

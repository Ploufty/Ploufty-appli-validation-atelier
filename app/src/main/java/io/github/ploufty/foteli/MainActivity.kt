package io.github.ploufty.foteli

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import io.github.ploufty.foteli.ui.AppViewModel
import io.github.ploufty.foteli.ui.FoteliApp
import io.github.ploufty.foteli.ui.FoteliTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Écran protégé (décision T3) : pas de captures, aperçu masqué dans les applis récentes.
        // Les versions de test l'autorisent pour produire les aperçus automatiques.
        if (!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        // Versions de test uniquement : classe de démonstration pour les captures automatiques.
        if (BuildConfig.DEBUG && savedInstanceState == null && intent.getBooleanExtra("demo", false)) {
            vm.seedDemo(intent.getStringExtra("screen"))
        }

        setContent {
            FoteliTheme { FoteliApp(vm) }
        }
    }
}

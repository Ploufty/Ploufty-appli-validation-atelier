package io.github.ploufty.foteli

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF1F2A44)
private val Blue = Color(0xFF2C5FD6)
private val Paper = Color(0xFFEEF3F9)
private val Green = Color(0xFF1E8E4E)
private val Red = Color(0xFFC23B32)

/** Une vérification affichée à l'écran : libellé + résultat réel, mesuré sur la tablette. */
data class Check(val label: String, val ok: Boolean)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Écran protégé (décision T3). Les versions de test l'autorisent pour produire les aperçus.
        val screenshotsBlocked = !BuildConfig.DEBUG
        if (screenshotsBlocked) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        val checks = listOf(
            Check(getString(R.string.check_installed), true),
            Check(getString(R.string.check_no_internet), !requestsPermission(Manifest.permission.INTERNET)),
            Check(
                getString(R.string.check_no_cloud_backup),
                (applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP) == 0,
            ),
            Check(
                getString(if (screenshotsBlocked) R.string.check_screenshots_blocked else R.string.check_screenshots_test),
                screenshotsBlocked,
            ),
        )

        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Blue, background = Paper)) {
                WelcomeScreen(checks)
            }
        }
    }

    private fun requestsPermission(permission: String): Boolean {
        val info = packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
        )
        return info.requestedPermissions?.contains(permission) == true
    }
}

@Composable
private fun WelcomeScreen(checks: List<Check>) {
    val config = LocalConfiguration.current
    Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
        Row(
            modifier = Modifier.padding(48.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Blue, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("F", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    stringResource(R.string.app_name),
                    color = Navy,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.welcome_version, BuildConfig.VERSION_NAME),
                    color = Blue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(stringResource(R.string.welcome_text), color = Navy, fontSize = 18.sp)
                Text(
                    stringResource(
                        R.string.device_info,
                        Build.MODEL,
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                        config.screenWidthDp,
                        config.screenHeightDp,
                    ),
                    color = Navy.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    stringResource(R.string.checks_title),
                    color = Navy,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                checks.forEach { CheckRow(it) }
                Text(stringResource(R.string.next_version), color = Navy.copy(alpha = 0.7f), fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun CheckRow(check: Check) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Symbole + couleur : l'information n'est jamais portée par la seule couleur.
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (check.ok) Green else Red, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (check.ok) "✓" else "✗", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.width(16.dp))
        Text(check.label, color = Navy, fontSize = 18.sp)
    }
}

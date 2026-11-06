package io.github.ploufty.foteli.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Autorisation de l'appareil photo, demandée uniquement dans l'espace enseignant (jamais devant un enfant). */
class CameraAccess(val allowed: Boolean, val request: () -> Unit, val openSystemSettings: () -> Unit)

val LocalCameraAccess = staticCompositionLocalOf { CameraAccess(true, {}, {}) }

private fun cameraAllowed(context: Context) =
    context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

@Composable
fun FoteliApp(vm: AppViewModel) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val students by vm.students.collectAsStateWithLifecycle()
    val workshops by vm.workshops.collectAsStateWithLifecycle()
    val traces by vm.traces.collectAsStateWithLifecycle()
    val freeMode = settings?.freeMode == true

    var allowed by remember { mutableStateOf(cameraAllowed(context)) }
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed = it }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { allowed = cameraAllowed(context) }
    val access = CameraAccess(
        allowed = allowed,
        request = { launcher.launch(Manifest.permission.CAMERA) },
        openSystemSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        },
    )
    // Premier passage dans l'espace enseignant (fin du premier lancement) : Android demande l'autorisation une fois.
    val inTeacherSpace = vm.screen.let {
        it is Screen.Teacher || it is Screen.EditStudent || it is Screen.EditWorkshop || it == Screen.BulkAdd ||
            it is Screen.WorkshopPhotos || it is Screen.StudentPhotos || it is Screen.PhotoView
    }
    LaunchedEffect(inTeacherSpace) {
        if (inTeacherSpace && !allowed && !asked) {
            asked = true
            access.request()
        }
    }

    BackHandler { vm.onBack() }

    CompositionLocalProvider(LocalCameraAccess provides access) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Paper)
                // Tout toucher compte comme une activité (retours automatiques après inactivité).
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                            vm.touch()
                        }
                    }
                },
        ) {
            when (val s = vm.screen) {
                Screen.Loading -> Unit
                Screen.Welcome -> WelcomeScreen { vm.go(Screen.ChoosePin(reset = false)) }
                is Screen.ChoosePin -> ChoosePinScreen(s, vm)
                is Screen.ConfirmPin -> ConfirmPinScreen(s, vm)
                is Screen.ShowRescue -> ShowRescueScreen(s, vm)
                Screen.ClassName -> ClassNameScreen(vm)
                Screen.Home -> HomeScreen(students, canPlay = allowed && (freeMode || workshops.any { it.active }), vm = vm)
                is Screen.ChooseWorkshop ->
                    ChooseWorkshopScreen(students.firstOrNull { it.id == s.studentId }, workshops, freeMode, vm)
                is Screen.Camera -> {
                    val workshop = workshops.firstOrNull { it.id == s.workshopId }
                    ChildCameraScreen(
                        state = s,
                        student = students.firstOrNull { it.id == s.studentId },
                        workshop = workshop,
                        front = if (s.workshopId == null) settings?.freeModeFrontCamera == true else workshop?.frontCamera == true,
                        cameraAllowed = allowed,
                        vm = vm,
                    )
                }
                is Screen.Review -> ReviewScreen(s, students.firstOrNull { it.id == s.studentId }, workshops.firstOrNull { it.id == s.workshopId }, vm)
                is Screen.Bravo -> BravoScreen(s, students.firstOrNull { it.id == s.studentId }, vm)
                Screen.PinEntry -> PinEntryScreen(settings, vm)
                Screen.RescueEntry -> RescueEntryScreen(settings, vm)
                is Screen.Teacher -> TeacherFrame(settings, s.tab, vm) {
                    when (s.tab) {
                        TeacherTab.CLASS -> ClassTab(students, traces, vm)
                        TeacherTab.WORKSHOPS -> WorkshopsTab(workshops, traces, freeMode, vm)
                        TeacherTab.PHOTOS -> PhotosTab(students, workshops, traces, vm)
                        TeacherTab.SETTINGS -> SettingsTab(settings, students, workshops.size, traces, vm)
                    }
                }
                is Screen.EditStudent -> TeacherFrame(settings, TeacherTab.CLASS, vm) {
                    EditStudentScreen(students.firstOrNull { it.id == s.studentId }, traces, vm)
                }
                Screen.BulkAdd -> TeacherFrame(settings, TeacherTab.CLASS, vm) { BulkAddScreen(vm) }
                is Screen.WorkshopPhotos -> TeacherFrame(settings, TeacherTab.PHOTOS, vm) {
                    WorkshopPhotosScreen(s, students, workshops.firstOrNull { it.id == s.workshopId }, traces, vm)
                }
                is Screen.StudentPhotos -> TeacherFrame(settings, TeacherTab.PHOTOS, vm) {
                    StudentPhotosScreen(s, students.firstOrNull { it.id == s.studentId }, workshops, traces, vm)
                }
                is Screen.PhotoView -> TeacherFrame(settings, TeacherTab.PHOTOS, vm, scroll = false) {
                    val trace = traces.firstOrNull { it.id == s.traceId }
                    PhotoViewScreen(s, trace, students.firstOrNull { it.id == trace?.studentId }, workshops.firstOrNull { it.id == trace?.workshopId }, vm)
                }
                is Screen.EditWorkshop -> TeacherFrame(settings, TeacherTab.WORKSHOPS, vm, scroll = false) {
                    EditWorkshopScreen(workshops.firstOrNull { it.id == s.workshopId }, workshops, traces, vm)
                }
            }
            vm.teacherCamera?.let { TeacherCameraOverlay(it, allowed, vm) }
        }
    }
}

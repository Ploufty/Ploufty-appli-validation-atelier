package io.github.ploufty.foteli.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FoteliApp(vm: AppViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val students by vm.students.collectAsStateWithLifecycle()

    BackHandler { vm.onBack() }

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
            Screen.Home -> HomeScreen(students, vm)
            is Screen.Child -> ChildScreen(students.firstOrNull { it.id == s.studentId }, vm)
            Screen.PinEntry -> PinEntryScreen(settings, vm)
            Screen.RescueEntry -> RescueEntryScreen(settings, vm)
            is Screen.Teacher -> TeacherFrame(settings, s.tab, vm) {
                when (s.tab) {
                    TeacherTab.CLASS -> ClassTab(students, vm)
                    TeacherTab.SETTINGS -> SettingsTab(settings, students, vm)
                }
            }
            is Screen.EditStudent -> TeacherFrame(settings, TeacherTab.CLASS, vm) {
                EditStudentScreen(students.firstOrNull { it.id == s.studentId }, vm)
            }
            Screen.BulkAdd -> TeacherFrame(settings, TeacherTab.CLASS, vm) { BulkAddScreen(vm) }
        }
    }
}

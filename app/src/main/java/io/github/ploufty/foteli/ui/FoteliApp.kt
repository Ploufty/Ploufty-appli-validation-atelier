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
    val workshops by vm.workshops.collectAsStateWithLifecycle()
    val freeMode = settings?.freeMode == true

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
            Screen.Home -> HomeScreen(students, canPlay = freeMode || workshops.any { it.active }, vm = vm)
            is Screen.ChooseWorkshop ->
                ChooseWorkshopScreen(students.firstOrNull { it.id == s.studentId }, workshops, freeMode, vm)
            is Screen.CameraSoon -> CameraSoonScreen(
                students.firstOrNull { it.id == s.studentId },
                workshops.firstOrNull { it.id == s.workshopId },
                vm,
                s.studentId,
            )
            Screen.PinEntry -> PinEntryScreen(settings, vm)
            Screen.RescueEntry -> RescueEntryScreen(settings, vm)
            is Screen.Teacher -> TeacherFrame(settings, s.tab, vm) {
                when (s.tab) {
                    TeacherTab.CLASS -> ClassTab(students, vm)
                    TeacherTab.WORKSHOPS -> WorkshopsTab(workshops, freeMode, vm)
                    TeacherTab.SETTINGS -> SettingsTab(settings, students, workshops.size, vm)
                }
            }
            is Screen.EditStudent -> TeacherFrame(settings, TeacherTab.CLASS, vm) {
                EditStudentScreen(students.firstOrNull { it.id == s.studentId }, vm)
            }
            Screen.BulkAdd -> TeacherFrame(settings, TeacherTab.CLASS, vm) { BulkAddScreen(vm) }
            is Screen.EditWorkshop -> TeacherFrame(settings, TeacherTab.WORKSHOPS, vm) {
                EditWorkshopScreen(workshops.firstOrNull { it.id == s.workshopId }, vm)
            }
        }
    }
}

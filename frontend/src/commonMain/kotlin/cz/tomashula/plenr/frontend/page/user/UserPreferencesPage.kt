package cz.tomashula.plenr.frontend.page.user

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.bsForm
import cz.tomashula.plenr.frontend.component.bsFormInput
import cz.tomashula.plenr.frontend.component.bsLabelledFormField
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.check.checkBox
import dev.kilua.html.*
import dev.kilua.html.helpers.TagStyleFun
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.panel.flexPanel
import kotlinx.serialization.Serializable

@Composable
fun IComponent.userPreferencesPage(viewModel: MainViewModel)
{
    val router = Router.current
    var preferences by remember { mutableStateOf<UserPreferencesDto?>(null) }

    if (viewModel.user?.isAdmin == true)
    {
        h2t("Admins do not have any preferences") {
            textAlign(TextAlign.Center)
            marginTop(50.px)
        }
        return
    }

    LaunchedEffect(Unit) {
        preferences = viewModel.getPreferences()
    }

    flexPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center
    ) {
        bsForm<PreferencesForm>(
            onSubmit = { data, _, _ ->
                viewModel.setPreferences(preferences!!)
                router.navigate(Route.HOME)
            }
        ) {
            LaunchedEffect(preferences) {
                preferences?.let { setData(it.toFormData()) }
            }
            bsLabelledFormField("Trainings per week") {
                bsFormInput(it, type = InputType.Number) {
                    bindCustom(PreferencesForm::trainingsPerWeek)
                    onChange {
                        preferences = preferences?.copy(trainingsPerWeek = value?.toInt() ?: 0)
                    }
                    attribute("min", "0")
                    attribute("max", "7")
                }
            }

                bsLabelledFormField("Notifications") {
                    table(id = it) {
                        width(100.perc)
                        style("border-collapse", "collapse")

                        thead {
                            tr {
                                notificationsTableHeader("Notification")
                                notificationsTableHeader("Email")
                                notificationsTableHeader("SMS") {
                                    color(Color.Gray)
                                }
                            }
                        }
                        tbody {
                            notificationsTableRow(
                                label = "Training Arranged",
                                email = preferences?.trainingArrangedNotiEmail == true,
                                sms = preferences?.trainingArrangedNotiSms == true,
                                onEmailChange = { preferences = preferences?.copy(trainingArrangedNotiEmail = it) },
                                onSmsChange = { preferences = preferences?.copy(trainingArrangedNotiSms = it) }
                            )
                            notificationsTableRow(
                                label = "Training Moved",
                                email = preferences?.trainingMovedNotiEmail == true,
                                sms = preferences?.trainingMovedNotiSms == true,
                                onEmailChange = { preferences = preferences?.copy(trainingMovedNotiEmail = it) },
                                onSmsChange = { preferences = preferences?.copy(trainingMovedNotiSms = it) }
                            )
                            notificationsTableRow(
                                label = "Training Cancelled",
                                email = preferences?.trainingCancelledNotiEmail == true,
                                sms = preferences?.trainingCancelledNotiSms == true,
                                onEmailChange = { preferences = preferences?.copy(trainingCancelledNotiEmail = it) },
                                onSmsChange = { preferences = preferences?.copy(trainingCancelledNotiSms = it) }
                            )
                        }
                    }

            }

            bsButton(label = "Save preferences", type = ButtonType.Submit, className = "mt-3")
        }
    }
}

@Composable
fun TagStyleFun.notificationsTableHeaderDataStyle()
{
    background(Color("#f3f3f3"))
    border(Border(width = 1.px, style = BorderStyle.Solid, color = Color("#ccc")))
    padding(10.px)
    textAlign(TextAlign.Center)
}

@Composable
fun IComponent.notificationsTableHeader(
    text: String,
    content: @Composable ITh.() -> Unit = {}
)
{
    th {
        notificationsTableHeaderDataStyle()
        background(Color("#f3f3f3"))
        +text
        content()
    }
}

@Composable
fun IComponent.notificationsTableData(
    content: @Composable IComponent.() -> Unit = {}
)
{
    td {
        notificationsTableHeaderDataStyle()
        content()
    }
}

@Composable
private fun IComponent.notificationsTableRow(
    label: String,
    email: Boolean,
    sms: Boolean,
    onEmailChange: (Boolean) -> Unit,
    onSmsChange: (Boolean) -> Unit
)
{
    tr {
        notificationsTableHeader(label)
        notificationsTableData {
            checkBox(value = email) {
                style("transform", "scale(1.5)")
                cursor(Cursor.Pointer)
                onChange { onEmailChange(this.value) }
            }
        }
        notificationsTableData {
            checkBox(value = sms) {
                style("transform", "scale(1.5)")
                disabled(true)
                // cursor(Cursor.Pointer)
                onChange { onSmsChange(this.value) }
            }
        }
    }
}

@Serializable
private data class PreferencesForm(
    val trainingsPerWeek: Int = 1,
    val trainingArrangedNotiEmail: Boolean = true,
    val trainingArrangedNotiSms: Boolean = true,
    val trainingMovedNotiEmail: Boolean = true,
    val trainingMovedNotiSms: Boolean = true,
    val trainingCancelledNotiEmail: Boolean = true,
    val trainingCancelledNotiSms: Boolean = true
)

private fun UserPreferencesDto.toFormData() = PreferencesForm(
    trainingsPerWeek = trainingsPerWeek,
    trainingArrangedNotiEmail = trainingArrangedNotiEmail,
    trainingArrangedNotiSms = trainingArrangedNotiSms,
    trainingMovedNotiEmail = trainingMovedNotiEmail,
    trainingMovedNotiSms = trainingMovedNotiSms,
    trainingCancelledNotiEmail = trainingCancelledNotiEmail,
    trainingCancelledNotiSms = trainingCancelledNotiSms
)

package me.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.text.text
import dev.kilua.html.div
import dev.kilua.html.label

@Composable
fun IComponent.formField(
    className: String = "",
    inputId: String,
    label: String,
    value: String,
    type: InputType = InputType.Text,
    required: Boolean = false,
    onChange: (String) -> Unit
)
{
    div(className = "form-field $className") {
        applyColumn()
        label(htmlFor = inputId, className = "form-field-label") {
            +label
        }
        text(value = value, type, id = inputId, className = "form-field-input", required = required) {
            onChange {
                onChange(this.value ?: "")
            }
        }
    }
}
package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.kilua.core.IComponent
import dev.kilua.form.*
import dev.kilua.form.text.IText
import dev.kilua.form.text.text
import dev.kilua.html.div
import dev.kilua.html.divt
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

@Composable
inline fun <reified T : Any> IComponent.bsForm(
    noinline onSubmitValid: suspend (T) -> Unit,
    setup: @Composable Form<T>.() -> Unit
)
{
    val coroutineScope = rememberCoroutineScope()

    form<T>(className = "needs-validation") {
        onSubmit {
            coroutineScope.launch {
                val isValid = this@form.validate()
                this@form.className = "was-validated"
                if (isValid)
                    onSubmitValid(this@form.getData())
            }
        }
        setup()
    }
}

@Composable
inline fun IComponent.bsLabelledFormField(
    label: String,
    wrapperClassName: String? = null,
    crossinline setup: @Composable IComponent.(inputId: String) -> Unit
)
{
    fieldWithLabel(label, className = "form-label", wrapperClassName = wrapperClassName) { inputId ->
        setup(inputId)
    }
}

@Composable
inline fun <K : Any> Form<K>.bsFormInput(
    id: String? = null,
    bindKey: KProperty1<K, String?>,
    type: InputType = InputType.Text,
    placeholder: String? = null,
    noinline validator: ((StringFormControl) -> Boolean)? = null,
    required: Boolean = true,
    crossinline setup: @Composable IText.() -> Unit = {}
)
{
    text(
        id = id,
        className = "form-control",
        type = type,
        required = required,
        placeholder = placeholder
    ) {
        bind(bindKey, validator)
        setup()
    }
}

@Composable
fun IComponent.bsInvalidFeedback(
    text: String
)
{
    divt(text = text, className = "invalid-feedback")
}

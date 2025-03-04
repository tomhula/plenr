package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.kilua.core.IComponent
import dev.kilua.form.*
import dev.kilua.form.text.IText
import cz.tomashula.plenr.frontend.component.onSubmit as onFormSubmit
import dev.kilua.form.text.text
import dev.kilua.html.data
import dev.kilua.html.divt
import kotlinx.coroutines.launch
import web.dom.events.Event
import kotlin.reflect.KProperty1

@Composable
inline fun <reified T : Any> IComponent.bsValidatedForm(
    noinline onSubmitValid: suspend (T) -> Unit,
    crossinline content: @Composable Form<T>.() -> Unit
)
{
    bsForm<T>(
        className = "needs-validation",
        onSubmit = { data, form, _ ->
            val isValid = form.validate()
            form.className = "was-validated"
            if (isValid)
                onSubmitValid(data)
        },
        content = content
    )
}

@Composable
inline fun <reified T : Any> IComponent.bsForm(
    className: String? = null,
    noinline onSubmit: suspend (data: T, form: Form<T>, event: Event) -> Unit,
    crossinline content: @Composable Form<T>.() -> Unit
)
{
    val coroutineScope = rememberCoroutineScope()

    form<T>(className = className) {
        onFormSubmit { event ->
            coroutineScope.launch {
                onSubmit(this@form.getData(), this@form, event)
            }
        }
        content()
    }
}

@Composable
inline fun <reified T : Any> IComponent.bsFormRef(
    className: String? = null,
    noinline onSubmit: suspend (data: T, form: Form<T>, event: Event) -> Unit,
    crossinline content: @Composable Form<T>.() -> Unit
): Form<T>
{
    val coroutineScope = rememberCoroutineScope()

    return formRef<T>(className = className) {
        onFormSubmit { event ->
            coroutineScope.launch {
                onSubmit(this@formRef.getData(), this@formRef, event)
            }
        }
        content()
    }
}

@Composable
inline fun IComponent.bsLabelledFormField(
    label: String,
    wrapperClassName: String? = null,
    groupClassName: String? = null,
    labelAfter: Boolean = false,
    crossinline content: @Composable IComponent.(inputId: String) -> Unit
)
{
    fieldWithLabel(
        label = label,
        className = "form-label",
        groupClassName = groupClassName,
        labelAfter = labelAfter,
        wrapperClassName = wrapperClassName,
        content = { inputId -> content(inputId) }
    )
}

@Composable
inline fun <K : Any> Form<K>.bsFormInput(
    id: String? = null,
    bindKey: KProperty1<K, String?>,
    type: InputType = InputType.Text,
    value: String? = null,
    placeholder: String? = null,
    required: Boolean = true,
    noinline validator: ((StringFormControl) -> Boolean)? = null,
    crossinline content: @Composable IText.() -> Unit = {}
)
{
    bsFormInput(
        id = id,
        type = type,
        value = value,
        placeholder = placeholder,
        required = required
    ) {
        bind(bindKey, validator)
        content()
    }
}

@Composable
inline fun IComponent.bsFormInput(
    id: String? = null,
    type: InputType = InputType.Text,
    value: String? = null,
    placeholder: String? = null,
    required: Boolean = true,
    crossinline content: @Composable IText.() -> Unit = {}
)
{
    text(
        id = id,
        className = "form-control",
        type = type,
        required = required,
        placeholder = placeholder,
        value = value
    ) {
        content()
    }
}

@Composable
fun IComponent.bsInvalidFeedback(
    text: String
)
{
    divt(text = text, className = "invalid-feedback")
}

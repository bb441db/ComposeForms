package github.bb441db.forms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

fun<T: Any> createMutableFormState(data: Commitable<T>): MutableState<FormData<T>> {
    return mutableStateOf(FormData(data))
}

@Composable
fun<T: Any> Form(data: Commitable<T>, block: @Composable FormDataScope<T>.() -> Unit) {
    val (value, onValueChange) = remember { mutableStateOf(FormData(data)) }
    Form(value, onValueChange, block)
}

@Composable
fun<T: Any> Form(mutableState: MutableState<FormData<T>>, block: @Composable FormDataScope<T>.() -> Unit) {
    val (value, onValueChange) = mutableState
    Form(value, onValueChange, block)
}

@Composable
fun<T: Any> Form(value: FormData<T>, onValueChange: (FormData<T>) -> Unit, block: @Composable FormDataScope<T>.() -> Unit) {
    block(FormDataScope(value, onValueChange))
}
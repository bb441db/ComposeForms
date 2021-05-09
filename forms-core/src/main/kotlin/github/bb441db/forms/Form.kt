package github.bb441db.forms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun<T: Any> Form(data: Commitable<T>, block: @Composable FormDataScope<T>.() -> Unit) {
    val scope = remember { FormDataScope(mutableStateOf(FormData(data))) }
    block(scope)
}
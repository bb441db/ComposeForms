package github.bb441db.forms

import androidx.compose.runtime.Composable
import kotlin.internal.Exact
import kotlin.reflect.KProperty1

@Composable
fun<T: Any, V> FormDataScope<T>.FormEntry(prop: KProperty1<T, @Exact V>, block: @Composable FormEntryScope<T, V>.() -> Unit) {
    block(FormEntryScope(this, prop))
}
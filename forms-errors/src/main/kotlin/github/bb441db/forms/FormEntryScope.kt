package github.bb441db.forms

import androidx.compose.runtime.Composable

fun <T: Any, V> FormEntryScope<T, V>.hasError(errors: KeyableError<T>, ignoreOnMutated: Boolean = false): Boolean {
    if (ignoreOnMutated) {
        return !mutated && errors.hasError(prop)
    }
    return errors.hasError(prop)
}

fun <T: Any, V> FormEntryScope<T, V>.errorMessage(errors: KeyableError<T>, ignoreOnMutated: Boolean = false): String? {
    if (ignoreOnMutated) {
        if (mutated) {
            return null
        }

        return errors.getMessage(prop)
    }
    return errors.getMessage(prop)
}

@Composable
fun <T: Any, V> FormEntryScope<T, V>.ErrorMessage(errors: KeyableError<T>, ignoreOnMutated: Boolean = false, block: @Composable (String) -> Unit) {
    val message = errorMessage(errors, ignoreOnMutated)
    if (message != null) {
        block(message)
    }
}
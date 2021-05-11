package github.bb441db.forms

import kotlin.internal.Exact
import kotlin.reflect.KProperty1

class FormEntryScope<T: Any, V> constructor(private val form: FormDataScope<T>, val prop: KProperty1<T, @Exact V>) {
    val value = form[prop]
    val mutated by lazy { form.didChange(prop) }

    fun resetter(): (() -> Unit) {
        return form.resetterOf(prop)
    }

    fun reset() {
        form.reset(prop)
    }

    fun mutator(): ((V) -> Unit) {
        return form.mutatorOf(prop)
    }

    fun mutate(value: V) {
        form.mutate(prop, value)
    }
}
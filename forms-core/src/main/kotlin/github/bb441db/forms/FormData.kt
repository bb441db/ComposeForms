package github.bb441db.forms

import kotlin.internal.Exact
import kotlin.reflect.KProperty1

internal data class FormData<T: Any>(
    val value: Commitable<T>,
    val initial: Commitable<T> = value,
) {
    val data = value.data

    internal fun sync(): FormData<T> {
        return this.copy(value = value, initial = value)
    }

    internal fun reset(): FormData<T> {
        return this.copy(value = initial, initial = initial)
    }

    internal fun didChange(): Boolean {
        return this.value.data != this.initial.data
    }

    operator fun<V> get(prop: KProperty1<T, @Exact V>): V {
        return prop.get(value.data)
    }

    internal fun<V> reset(prop: KProperty1<T, @Exact V>): FormData<T> {
        return this.commit(prop, prop.get(initial.data))
    }

    internal fun<V> didChange(prop: KProperty1<T, @Exact V>): Boolean {
        return prop.get(initial.data) != prop.get(value.data)
    }

    internal fun<V> commit(prop: KProperty1<T, @Exact V>, value:  @Exact V): FormData<T> {
        return this.copy(value = this.value.commit(prop, value), initial = initial)
    }
}
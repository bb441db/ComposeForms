package github.bb441db.forms

import kotlin.internal.Exact
import kotlin.reflect.KProperty1

interface Commitable<T: Any> {
    val state: T
    fun<V> commit(prop: KProperty1<T, V>, newValue: @Exact V): Commitable<T>
}
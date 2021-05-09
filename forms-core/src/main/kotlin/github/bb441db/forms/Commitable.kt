package github.bb441db.forms

import kotlin.internal.Exact
import kotlin.reflect.KProperty1

interface Commitable<T: Any> {
    val data: T
    fun<V> commit(prop: KProperty1<T, V>, value: @Exact V): Commitable<T>
}
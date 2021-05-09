package github.bb441db.forms

import kotlin.reflect.KProperty1

interface KeyableError<T: Any> {
    fun<V> getMessage(prop: KProperty1<T, V>): String?

    fun<V: Any> getErrors(prop: KProperty1<T, V>): KeyableError<V>? {
        return null
    }

    fun<V> hasError(prop: KProperty1<T, V>): Boolean {
        return !this.getMessage(prop).isNullOrEmpty()
    }
}
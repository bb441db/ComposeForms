package github.bb441db.forms

import androidx.compose.runtime.MutableState
import kotlin.internal.Exact
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class FormDataScope<T: Any> internal constructor(private val state: MutableState<FormData<T>>) {
    /**
     * Current states data.
     */
    val data: T get() = state.value.data

    /**
     * True when current state is not equal to the initial state.
     */
    val mutated: Boolean get() = state.value.didChange()

    /**
     * Gets the value for the given property.
     */
    operator fun<V> get(prop: KProperty1<T, @Exact V>): V {
        return prop.get(state.value.data)
    }

    /**
     * Checks if the current value of the given property is not equal to the initial value.
     */
    fun<V> didChange(prop: KProperty1<T, @Exact V>): Boolean {
        return state.value.didChange(prop)
    }

    /**
     * Returns a mutator function for the given property.
     */
    fun<V> mutatorOf(prop: KProperty1<T, @Exact V>): ((V) -> Unit) {
        return {
            mutate(prop, it)
        }
    }

    /**
     * Returns a reset function for the given property.
     */
    fun<V> resetterOf(prop: KProperty1<T, @Exact V>): (() -> Unit) {
        return {
            reset(prop)
        }
    }

    /**
     * Mutates the value of the given property to the given value.
     */
    fun<V> mutate(prop: KProperty1<T, @Exact V>, value: @Exact V) {
        state.value = state.value.commit(prop, value)
    }

    /**
     * Resets the value of the given property to the initial value.
     */
    fun<V> reset(prop: KProperty1<T, @Exact V>) {
        state.value = state.value.reset(prop)
    }

    /**
     * Synchronizes the initial state with the current state.
     */
    fun sync() {
        state.value = state.value.sync()
    }

    /**
     * Resets the current state to the initial state.
     */
    fun reset() {
        state.value = state.value.reset()
    }
}
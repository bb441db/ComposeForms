package github.bb441db.example.data

import github.bb441db.example.utils.firstOrNull
import github.bb441db.forms.KeyableError
import kotlin.reflect.KProperty1

data class ExampleErrors(private val errors: Map<String, Array<String>>): KeyableError<Example> {
    override fun <V> getMessage(prop: KProperty1<Example, V>): String? {
        return errors.firstOrNull(prop.name)
    }
}
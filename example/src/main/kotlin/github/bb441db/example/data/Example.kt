package github.bb441db.example.data

import github.bb441db.forms.Commitable
import kotlin.reflect.KProperty1

data class Example(val foo: Boolean, val bar: String?, val fooBar: String?) : Commitable<Example> {
    override val data: Example = this

    override fun <V> commit(prop: KProperty1<Example, V>, value: V): Commitable<Example> {
        return when (prop) {
            Example::foo -> this.copy(foo = value as Boolean)
            Example::bar -> this.copy(bar = value as String?)
            Example::fooBar -> this.copy(fooBar = value as String?)
            else -> this
        }
    }
}


package github.bb441db.forms

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FormsProcessorTests : AbstractCompileTests() {
    @Test
    fun `Generated class contains required methods`() {
        val clazz = compile(
            fileName = "Example",
            source = """                
                import github.bb441db.forms.annotations.Commitable
                
                @Commitable(fileName = "Example", className = "Generated")
                data class Example(val foo: Boolean)
            """
        ).classLoader.loadClass("Generated")

        assertThat(clazz).hasDeclaredMethods("commit")
        assertThat(clazz).hasDeclaredMethods("equals")
        assertThat(clazz).hasDeclaredMethods("toString")
    }

    @Test
    fun `Simple data class`() {
        val result = compile(
            fileName = "Example",
            source = """                
                import github.bb441db.forms.annotations.Commitable
                
                @Commitable(fileName = "Example", className = "Generated")
                data class Example(val foo: Boolean, val bar: String? = null)
            """
        ).sourceWithName("Example")

        assertThat(result).contains("Example::foo -> copy(state = state.copy(foo = newValue as Boolean))")
        assertThat(result).contains("Example::bar -> copy(state = state.copy(bar = newValue as String?))")
    }

    @Test
    fun `Class with type parameters`() {
        val result = compile(
            fileName = "Example",
            source = """                
                import github.bb441db.forms.annotations.Commitable
                
                @Commitable(fileName = "Example", className = "Generated")
                data class Example<T : Any>(val foo: T, val bar: T?)
            """
        ).sourceWithName("Example")

        assertThat(result).contains("data class Generated<T : Any>")
        assertThat(result).contains("override val state: Example<T>")
        assertThat(result).contains(": Commitable<Example<T>>")
        assertThat(result).contains("@Suppress(\"UNCHECKED_CAST\")")
        assertThat(result).contains("Example<T>::foo -> copy(state = state.copy(foo = newValue as T))")
        assertThat(result).contains("Example<T>::bar -> copy(state = state.copy(bar = newValue as T?))")
    }
}
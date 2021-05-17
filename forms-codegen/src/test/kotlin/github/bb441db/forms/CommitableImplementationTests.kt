package github.bb441db.forms

import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class CommitableImplementationTests : AbstractCompileTests() {
    private fun compileTestClasses(): KotlinCompilation.Result {
        val path = Paths.get("src", "test", "kotlin", "github", "bb441db", "forms", "TestClasses.kt")
        return compile(path.toFile())
    }

    @Suppress("UNCHECKED_CAST")
    private fun<T: Any> commitable(clazz: KClass<T>, state: T): Commitable<T> {
        val commitableClazz = compileTestClasses()
            .classLoader
            .loadClass("github.bb441db.forms.Commitable${clazz.simpleName}Impl") as Class<Commitable<T>>
        val constructor = commitableClazz.declaredConstructors.first()
        return constructor.newInstance(state) as Commitable<T>
    }

    private inline fun <reified T: Any> commitable(state: T) = commitable(T::class, state)

    @Test
    fun `Test 1`() {
        val instance = commitable(Test1(foo = true))
        val updated = instance.commit(Test1::foo, false)

        assertFalse(updated.state.foo)
        assertNotEquals(instance, updated)
    }

    @Test
    fun `Test 2`() {
        data class Test(val bar: Boolean)
        val instance = commitable(Test2(Test(false)))
        val updated = instance.commit(Test2<Test>::foo, Test(true))

        assertEquals(updated.state.foo, Test(true))
        assertNotEquals(instance, updated)
    }

    @Test
    fun `Test 3`() {
        val instance = commitable(Test3(foo = "foo", bar = "bar"))
        val updated = instance
            .commit(Test3::foo, "bar")
            .commit(Test3::bar, "foo")

        assertEquals(updated.state.foo, "bar")
        assertEquals(updated.state.bar, "foo")
        assertNotEquals(instance, updated)
    }
}
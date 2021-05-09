package github.bb441db.example

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import github.bb441db.example.data.Example
import github.bb441db.example.data.ExampleErrors
import github.bb441db.example.ui.theme.FormsTheme
import github.bb441db.forms.*
import kotlin.reflect.KProperty1

@Composable
fun ExampleForm(initial: Example = Example(false, "", "")) {
    val (errors, setErrors) = remember { mutableStateOf(ExampleErrors(emptyMap())) }
    Column {
        Form(initial) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FooFormEntry()
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextFieldFormEntry(Example::bar, errors, ignoreOnMutated = true)

            Spacer(modifier = Modifier.height(24.dp))

            TextFieldFormEntry(Example::fooBar, errors)

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Button(onClick = ::sync, enabled = mutated) {
                    Text("Sync")
                }

                Spacer(modifier = Modifier.width(24.dp))

                Button(onClick = ::reset, enabled = mutated) {
                    Text("Reset")
                }

                Spacer(modifier = Modifier.width(24.dp))

                Button(
                    onClick = {
                        sync()
                        setErrors(ExampleErrors(mapOf(
                            "bar" to arrayOf("Error message for bar"),
                            "fooBar" to arrayOf("Error message for fooBar"),
                        )))
                    }
                ) {
                    Text("Post error")
                }
            }
        }
    }
}

@Composable
private fun FormDataScope<Example>.FooFormEntry() {
    FormEntry(prop = Example::foo) {
        Switch(
            checked = value,
            onCheckedChange = mutator()
        )

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(onClick = resetter(), enabled = mutated) {
            val opacity = if (mutated) 1f else .3f
            Icon(
                modifier = Modifier.alpha(opacity),
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset foo"
            )
        }
    }
}

@Composable
private fun<T: Any> FormDataScope<T>.TextFieldFormEntry(prop: KProperty1<T, String?>, errors: KeyableError<T>, ignoreOnMutated: Boolean = false) {
    FormEntry(prop = prop) {
        Column {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(1f),
                value = value.orEmpty(),
                label = { Text("Type something") },
                onValueChange = mutator(),
                isError = hasError(errors, ignoreOnMutated = ignoreOnMutated),
                trailingIcon = {
                    IconButton(onClick = resetter(), enabled = mutated) {
                        val opacity = if (mutated) 1f else 0f
                        Icon(
                            modifier = Modifier.alpha(opacity),
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset"
                        )
                    }
                }
            )

            ErrorMessage(errors, ignoreOnMutated = ignoreOnMutated) {
                Text(text = it, color = MaterialTheme.colors.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExampleFormPreview() {
    FormsTheme {
        Box(Modifier.padding(24.dp)) {
            ExampleForm()
        }
    }
}
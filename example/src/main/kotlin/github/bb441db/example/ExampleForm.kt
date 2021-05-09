package github.bb441db.example

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import github.bb441db.example.data.Example
import github.bb441db.example.ui.theme.FormsTheme
import github.bb441db.forms.Form
import github.bb441db.forms.FormDataScope
import github.bb441db.forms.FormEntry

@Composable
fun ExampleForm(initial: Example = Example(false, null)) {
    Column {
        Form(initial) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FooFormEntry()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                BarFormEntry()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Button(onClick = ::sync, enabled = mutated) {
                    Text("Sync")
                }

                Spacer(modifier = Modifier.width(24.dp))

                Button(onClick = ::reset, enabled = mutated) {
                    Text("Reset")
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
private fun FormDataScope<Example>.BarFormEntry() {
    FormEntry(prop = Example::bar) {
        TextField(
            value = value.orEmpty(),
            label = { Text("Type something") },
            onValueChange = mutator()
        )

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(onClick = resetter(), enabled = mutated) {
            val opacity = if (mutated) 1f else .3f
            Icon(
                modifier = Modifier.alpha(opacity),
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset bar"
            )
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
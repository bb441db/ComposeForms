package github.bb441db.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import github.bb441db.example.data.Example
import github.bb441db.example.ui.theme.FormsTheme
import github.bb441db.forms.createMutableFormState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FormsTheme {
                Scaffold(
                    topBar = { FormsTopBar() },
                ) {
                    val (form, setForm) = remember { createMutableFormState(Example(false, "", "")) }
                    Box(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                        ExampleForm(value = form, onValueChange = setForm)
                    }
                }
            }
        }
    }
}

@Composable
fun FormsTopBar() {
    TopAppBar(title = { Text(text = "Compose Forms Example") })
}
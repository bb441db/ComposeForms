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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import github.bb441db.example.ui.theme.FormsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FormsTheme {
                Scaffold(
                    topBar = { FormsTopBar() },
                ) {
                    Box(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                        ExampleForm()
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
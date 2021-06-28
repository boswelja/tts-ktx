package com.example.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scaffoldState = rememberScaffoldState()
            val coroutineScope = rememberCoroutineScope()

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(R.string.app_name))
                            }
                        )
                    },
                    scaffoldState = scaffoldState
                ) {
                    MainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .padding(vertical = 8.dp),
                        onShowSnackbar = {
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = it,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onShowSnackbar: (String) -> Unit
) {
    val viewModel: MainViewModel = viewModel()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            content = { Text(stringResource(R.string.hello_world)) },
            onClick = {
                viewModel.helloWorldSynthesis(onShowSnackbar)
            }
        )
        Button(
            content = { Text(stringResource(R.string.initial_delay)) },
            onClick = {
                viewModel.initialDelaySynthesis(onShowSnackbar)
            }
        )
        Button(
            content = { Text(stringResource(R.string.chaining_calls)) },
            onClick = {
                viewModel.chainedSynthesis(onShowSnackbar)
            }
        )
    }
}

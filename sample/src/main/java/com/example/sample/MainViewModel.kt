package com.example.sample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.tts.withTextToSpeech
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    fun helloWorldSynthesis(onShowSnackbar: (String) -> Unit) {
        val context = getApplication<Application>()
        val text = context.getString(R.string.hello_world)
        viewModelScope.launch {
            context.withTextToSpeech {
                onShowSnackbar(context.getString(R.string.starting, text))
                speak(text)
            }
        }
    }

    fun initialDelaySynthesis(onShowSnackbar: (String) -> Unit) {
        val context = getApplication<Application>()
        val text = context.getString(R.string.initial_delay)
        viewModelScope.launch {
            context.withTextToSpeech {
                delay(1000)
                onShowSnackbar(context.getString(R.string.starting, text))
                speak(text)
            }
        }
    }

    fun chainedSynthesis(onShowSnackbar: (String) -> Unit) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            context.withTextToSpeech {
                onShowSnackbar(
                    context.getString(R.string.starting, context.getString(R.string.chaining_calls))
                )
                speak(context.getString(R.string.please_wait))
                delay(1500)
                speak(context.getString(R.string.all_done))
            }
        }
    }
}

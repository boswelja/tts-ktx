package com.example.sample

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.tts.Result
import com.boswelja.tts.TextToSpeech
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var ttsReady by mutableStateOf(false)

    var tts: TextToSpeech? = null

    fun initTts(): Boolean {
        return try {
            tts = TextToSpeech(getApplication())
            ttsReady = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * A wrapper function for [TextToSpeech.speak] that calls a provided function with a result
     * message.
     */
    suspend fun doSynthesis(text: String, onShowSnackbar: (String) -> Unit) {
        val context = getApplication<Application>()
        onShowSnackbar(
            context.getString(R.string.starting, text)
        )
        when (tts!!.speak(text)) {
            Result.SUCCESS -> onShowSnackbar(
                context.getString(R.string.finished, text)
            )
            Result.FAILED -> onShowSnackbar(
                context.getString(R.string.failed, text)
            )
        }
    }

    fun helloWorldSynthesis(onShowSnackbar: (String) -> Unit) {
        val context = getApplication<Application>()
        val text = context.getString(R.string.hello_world)
        viewModelScope.launch {
            doSynthesis(text, onShowSnackbar)
        }
    }

    fun initialDelaySynthesis(onShowSnackbar: (String) -> Unit) {
        val context = getApplication<Application>()
        val text = context.getString(R.string.initial_delay)
        viewModelScope.launch {
            delay(1000)
            doSynthesis(text, onShowSnackbar)
        }
    }

    fun chainedSynthesis(onShowSnackbar: (String) -> Unit) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            // Do the first synthesis part
            doSynthesis(
                context.getString(R.string.please_wait),
                onShowSnackbar
            )

            // Wait for a bit. Your app could be waiting on a result
            delay(1500)

            // Continue speech
            doSynthesis(
                context.getString(R.string.all_done),
                onShowSnackbar
            )
        }
    }
}

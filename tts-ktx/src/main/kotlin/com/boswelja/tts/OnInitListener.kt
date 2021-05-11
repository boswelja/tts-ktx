package com.boswelja.tts

import android.speech.tts.TextToSpeech

internal class OnInitListener : TextToSpeech.OnInitListener {
    override fun onInit(status: Int) {
        // If init wasn't successful, we throw an exception.
        if (status != TextToSpeech.SUCCESS)
            throw IllegalStateException("Failed to init TextToSpeech")
    }
}

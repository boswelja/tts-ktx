package com.boswelja.tts

import android.speech.tts.TextToSpeech

internal class OnInitListener : TextToSpeech.OnInitListener {
    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS)
            throw IllegalStateException("Failed to init TextToSpeech")
    }
}

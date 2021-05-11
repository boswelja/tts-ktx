package com.boswelja.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class TextToSpeech(context: Context) : TextToSpeech(context, OnInitListener()) {

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + coroutineJob)
    private val uidCreator = UIDCreator()

    private val currentQueue = mutableMapOf<String, Channel<Result>>()

    private val utteranceProgressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) { }

        override fun onDone(utteranceId: String?) {
            coroutineScope.launch {
                currentQueue[utteranceId]?.send(Result.SUCCESS)
            }
        }

        override fun onError(utteranceId: String?) {
            coroutineScope.launch {
                currentQueue[utteranceId]?.send(Result.FAILED)
            }
        }
    }

    init {
        setOnUtteranceProgressListener(utteranceProgressListener)
    }

    suspend fun playEarcon(
        earcon: String,
        queueMode: Int = QUEUE_ADD,
        params: Bundle? = null
    ): Result {
        val utteranceId = uidCreator.next()
        val queueResult = playEarcon(
            earcon,
            queueMode,
            params,
            utteranceId
        )

        if (queueResult == SUCCESS) {
            val channel = Channel<Result>()
            currentQueue[utteranceId] = channel
            return channel.receive()
        }

        return Result.FAILED
    }

    suspend fun playSilentUtterance(
        duration: Long,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
        queueMode: Int = QUEUE_ADD
    ): Result {
        val utteranceId = uidCreator.next()
        val queueResult = playSilentUtterance(
            TimeUnit.MILLISECONDS.convert(duration, timeUnit),
            queueMode,
            utteranceId
        )

        if (queueResult == SUCCESS) {
            val channel = Channel<Result>()
            currentQueue[utteranceId] = channel
            return channel.receive()
        }

        return Result.FAILED
    }

    suspend fun speak(
        text: String,
        queueMode: Int = QUEUE_ADD,
        params: Bundle? = null
    ): Result {
        if (text.length > getMaxSpeechInputLength())
            throw IllegalArgumentException("text exceeds the max length allowed for speech")

        val utteranceId = uidCreator.next()
        val queueResult = speak(
            text,
            queueMode,
            params,
            utteranceId
        )

        if (queueResult == SUCCESS) {
            val channel = Channel<Result>()
            currentQueue[utteranceId] = channel
            return channel.receive()
        }

        return Result.FAILED
    }

    suspend fun synthesizeToFile(
        text: CharSequence,
        file: File,
        params: Bundle? = null,
    ): Result {
        if (text.length > getMaxSpeechInputLength())
            throw IllegalArgumentException("text exceeds the max length allowed for speech")

        val utteranceId = uidCreator.next()
        val queueResult = synthesizeToFile(
            text,
            params,
            file,
            utteranceId
        )

        if (queueResult == SUCCESS) {
            val channel = Channel<Result>()
            currentQueue[utteranceId] = channel
            return channel.receive()
        }

        return Result.FAILED
    }

    override fun stop(): Int {
        coroutineScope.launch {
            currentQueue.values.forEach {
                it.send(Result.FAILED)
            }
        }
        return super.stop()
    }

    override fun shutdown() {
        coroutineJob.cancel()
        super.shutdown()
    }
}

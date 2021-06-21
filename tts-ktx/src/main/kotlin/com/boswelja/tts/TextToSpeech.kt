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

/**
 * A wrapper class for [TextToSpeech], with additional functions for improved Kotlin support.
 * @param context [Context].
 */
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

    /**
     * Plays the earcon using the specified queueing mode and parameters.
     * @param earcon The earcon to play.
     * @param queueMode The queueing mode. See [QUEUE_ADD] and [QUEUE_FLUSH].
     * @param params Optional parameters for the request. Supported parameter names:
     * Engine#KEY_PARAM_STREAM, Engine specific parameters may be passed in but the parameter
     * keys must be prefixed by the name of the engine they are intended for. For example the keys
     * "com.svox.pico_foo" and "com.svox.pico:bar" will be passed to the engine named
     * "com.svox.pico" if it is being used.
     * @return See [Result].
     */
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

    /**
     * Plays silence for the specified amount of time using the specified queue mode.
     * @param duration The duration of the silence.
     * @param timeUnit The [TimeUnit] for the given duration.
     * @param queueMode The queueing mode. See [QUEUE_ADD] and [QUEUE_FLUSH].
     * @return See [Result].
     */
    @Deprecated("Use Kotlin's delay(millis) function instead")
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

    /**
     * Speaks the text using the specified queuing strategy and speech parameters, the text may be
     * spanned with TtsSpans.
     * @param text The string of text to be spoken. If longer than getMaxSpeechInputLength(),
     * [IllegalArgumentException] is thrown.
     * @param queueMode The queueing mode. See [QUEUE_ADD] and [QUEUE_FLUSH].
     * @param params Optional parameters for the request. Supported parameter names:
     * Engine#KEY_PARAM_STREAM, Engine#KEY_PARAM_VOLUME, Engine#KEY_PARAM_PAN. Engine specific
     * parameters may be passed in but the parameter keys must be prefixed by the name of the
     * engine they are intended for. For example the keys "com.svox.pico_foo" and
     * "com.svox.pico:bar" will be passed to the engine named "com.svox.pico" if it is being used.
     * @return See [Result].
     */
    suspend fun speak(
        text: CharSequence,
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

    /**
     * Synthesizes the given text to a [File] using the specified parameters.
     * @param text The string of text to be spoken. If longer than getMaxSpeechInputLength(),
     * [IllegalArgumentException] is thrown.
     * @param file File to write the generated audio data to.
     * @param params Optional parameters for the request. Engine specific parameters may be passed
     * in but the parameter keys must be prefixed by the name of the engine they are intended for.
     * For example the keys "com.svox.pico_foo" and "com.svox.pico:bar" will be passed to the
     * engine named "com.svox.pico" if it is being used.
     * @return See [Result].
     */
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
        // Send failure to any pending jobs in the queue
        coroutineScope.launch {
            currentQueue.values.forEach {
                it.send(Result.FAILED)
            }
        }
        return super.stop()
    }

    override fun shutdown() {
        // Cancel any coroutines running
        coroutineJob.cancel()
        super.shutdown()
    }
}

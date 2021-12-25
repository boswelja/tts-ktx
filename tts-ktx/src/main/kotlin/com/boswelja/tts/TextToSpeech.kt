package com.boswelja.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech.SUCCESS
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.io.File
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch

/**
 * Executes [actions] with an initialized [TextToSpeech] instance.
 */
public suspend fun <R> Context.withTextToSpeech(
    voicePitch: Float = 1.0f,
    speechRate: Float = 1.0f,
    language: Locale = Locale.getDefault(),
    voice: Voice? = null,
    actions: suspend TextToSpeech.() -> R
): R {
    // Get TTS instance
    val tts = getTextToSpeech(
        voicePitch, speechRate, language, voice
    )

    // Execute user actions
    val result = tts.actions()

    // Shut down TTS instance when we're done.
    tts.shutdown()

    return result
}

/**
 * Suspends until a [TextToSpeech] instance is successfully initialized. Note using this requires
 * you to call [TextToSpeech.shutdown] when you've finished.
 */
public suspend fun Context.getTextToSpeech(
    voicePitch: Float = 1.0f,
    speechRate: Float = 1.0f,
    language: Locale = Locale.getDefault(),
    voice: Voice? = null
): TextToSpeech {
    // Initialize TTS
    val channel = Channel<Boolean>()
    val tts = TextToSpeech(this) { initResult ->
        channel.trySendBlocking(initResult == SUCCESS)
    }
    val initSuccess = channel.receive()

    // Check init was successful
    check(initSuccess)

    // Set up this TTS instance
    tts.setPitch(voicePitch)
    tts.setSpeechRate(speechRate)
    voice?.let { tts.setVoice(voice) }
    tts.language = language

    return tts
}

/**
 * A wrapper class for [TextToSpeech], with additional functions for improved Kotlin support.
 * @param context [Context].
 */
public class TextToSpeech(
    context: Context,
    onInitListener: OnInitListener
) : android.speech.tts.TextToSpeech(context, onInitListener) {

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + coroutineJob)
    private val uidCreator = UIDCreator()

    private val currentQueue = mutableMapOf<String, Channel<Result>>()

    private val utteranceProgressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            // We don't need to do anything here
        }

        override fun onDone(utteranceId: String?) {
            coroutineScope.launch {
                currentQueue[utteranceId]?.send(Result.SUCCESS)
            }
        }

        override fun onError(utteranceId: String?) {
            // Ignore this since it's deprecated
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            coroutineScope.launch {
                currentQueue[utteranceId]?.send(Result.FAILED)
            }
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            coroutineScope.launch {
                val result = if (interrupted) {
                    Result.STOPPED
                } else {
                    Result.FLUSHED
                }
                currentQueue[utteranceId]?.send(result)
            }
        }
    }

    init {
        setOnUtteranceProgressListener(utteranceProgressListener)
    }

    /**
     * Plays the earcon using the specified queueing mode and parameters.
     * @param earcon The name of the earcon to play.
     * @param queueMode The queueing mode. See [android.speech.tts.TextToSpeech.QUEUE_ADD] and
     * [android.speech.tts.TextToSpeech.QUEUE_FLUSH].
     * @param params Optional parameters for the request. Supported parameter names:
     * Engine#KEY_PARAM_STREAM, Engine specific parameters may be passed in but the parameter
     * keys must be prefixed by the name of the engine they are intended for. For example the keys
     * "com.svox.pico_foo" and "com.svox.pico:bar" will be passed to the engine named
     * "com.svox.pico" if it is being used.
     * @return See [Result].
     */
    public suspend fun playEarcon(
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
     * Speaks the text using the specified queuing strategy and speech parameters, the text may be
     * spanned with TtsSpans.
     * @param text The string of text to be spoken. If longer than getMaxSpeechInputLength(),
     * [IllegalArgumentException] is thrown.
     * @param queueMode The queueing mode. See [android.speech.tts.TextToSpeech.QUEUE_ADD] and
     * [android.speech.tts.TextToSpeech.QUEUE_FLUSH].
     * @param params Optional parameters for the request. Supported parameter names:
     * Engine#KEY_PARAM_STREAM, Engine#KEY_PARAM_VOLUME, Engine#KEY_PARAM_PAN. Engine specific
     * parameters may be passed in but the parameter keys must be prefixed by the name of the
     * engine they are intended for. For example the keys "com.svox.pico_foo" and
     * "com.svox.pico:bar" will be passed to the engine named "com.svox.pico" if it is being used.
     * @return See [Result].
     */
    public suspend fun speak(
        text: CharSequence,
        queueMode: Int = QUEUE_ADD,
        params: Bundle? = null
    ): Result {
        // Check text length is less than the max
        require(text.length <= getMaxSpeechInputLength())

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
    public suspend fun synthesizeToFile(
        text: CharSequence,
        file: File,
        params: Bundle? = null,
    ): Result {
        // Check text length is less than the max
        require(text.length <= getMaxSpeechInputLength())

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

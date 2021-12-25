package com.boswelja.tts

/**
 * [TextToSpeech] job results.
 */
public enum class Result {
    /**
     * Indicates the operation was successful.
     */
    SUCCESS,

    /**
     * Indicates the operation failed.
     */
    FAILED,

    /**
     * Indicates the utterance was stopped, likely due to calling [TextToSpeech.stop].
     */
    STOPPED,

    /**
     * Indicates the utterance was flushed from the queue, likely due to queueing an utterance with
     * [android.speech.tts.TextToSpeech.QUEUE_FLUSH].
     */
    FLUSHED
}

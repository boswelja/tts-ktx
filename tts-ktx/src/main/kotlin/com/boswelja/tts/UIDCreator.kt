package com.boswelja.tts

import java.util.UUID

/**
 * A class for creating unique strings.
 */
internal class UIDCreator {

    /**
     * Get the next unique string.
     */
    fun next(): String = UUID.randomUUID().toString()
}

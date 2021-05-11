package com.boswelja.tts

import java.util.UUID

internal class UIDCreator {

    fun next(): String = UUID.randomUUID().toString()
}

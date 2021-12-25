package com.boswelja.tts

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

public class UIDCreatorTest {

    private lateinit var uidCreator: UIDCreator

    @Before
    public fun setUp() {
        uidCreator = UIDCreator()
    }

    @Test
    public fun next_isAlwaysUnique() {
        // Create a ridiculous number of UIDs
        val testSize = 10000
        val uids = (0 until testSize).map { uidCreator.next() }
        assertEquals(testSize, uids.distinct().count())
    }
}
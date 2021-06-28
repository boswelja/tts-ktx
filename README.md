# tts-ktx
A Kotlin-friendly wrapper for Android's TextToSpeech class.

## Usage

Add tts-ktx to your project with:
```kotlin
implementation("io.github.boswelja.tts-ktx:tts-ktx:$latestVersion")
```

Make use of `withTextToSpeech`:
```kotlin
coroutineScope.launch {
    context.withTextToSpeech {
        // Calls here are scoped to TextToSpeech, so you're free to make calls as you please
        speak("Hello, World!")
    }
}
```

From here, you can use similar functions to the standard API. The key differences being:
 * tts-ktx uses default parameter values for anything you don't pass to functions
 * All functions are suspendable, and will return a result when they complete.

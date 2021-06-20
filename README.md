# tts-ktx
A Kotlin-friendly wrapper for Android's TextToSpeech class.

## Usage

Add tts-ktx to your project with:
```kotlin
implementation("io.github.boswelja.tts-ktx:tts-ktx:$latestVersion")
```

Create an instance of `TextToSpeech`:
```kotlin
import com.boswelja.tts.TextToSpeech

val tts = TextToSpeech(context)
coroutineScope.launch {
    val result = tts.speak("Hello World!")
}
```

From here, you can use similar functions to the standard API. The key differences being:
 * tts-ktx uses default parameter values for anything you don't pass to functions
 * All functions are suspendable, and will return a result when they complete.
# Demo app of unified Robolectric/Instrumentation test

This project provides a basic example of a UI test that can be run both on
Robolectric and on an emulator or device.

The app's UI is in compose.

The app's structure is like this:

```
┌───────────────┐                       ┌───────────────┐                  ┌──────────────┐
│ MainActivity  │                       │ SecondActivity│                  │MainActivity  │
│               │                       │               │                  │              │
│ Label         │                       │ Text input    │                  │Label         │
│ ""            │                       │ "Hello"       │                  │"Hello"       │
│               │                       │               │                  │              │
│               │                       │               │                  │              │
│               │                       │               │                  │              │
│ Button        │      startActivity    │ Button        │     finish       │Button        │
│ "Click me"    ├──────────────────────►│ "Close"       ├─────────────────►│"Click me"    │
│               │       ForResult       │               │                  │              │
│               │                       │               │                  │              │
└───────────────┘                       └───────────────┘                  └──────────────┘
```

The `MainActivity` has a label (initially blank) and a button "Click me".
When the user clicks on "Click me", `MainActivity` launches `SecondActivity` with
`startActivityForResult().`

In `SecondActivity`, the user can type some text in a text input. When they click
the button "Close", `SecondActivity` sets the result with an `Intent` containing
the contents of the entered text in an intent extra. `SecondActivity` finishes.

When the control returns to `MainActivity`, its activity result callback extracts
the text from the intent extras, and sets the label with this text.

## Tests

There is one test `SharedUnitTest`, in `src/sharedTest/java`. It uses
[AndroidComposeTestRule](https://developer.android.com/jetpack/compose/testing#componentactivity)
to launch `MainActivity` and complete the flow described above.

This type of test works by default when run as an instrumented test on an emulator or a device.

On Robolectric however, by default the test would fail: After clicking on the "Click me" button
in `MainActivity`, Robolectric doesn't actually launch `SecondActivity`. Any interactions in tests
with views in `SecondActivity` would therefore fail.

### Solution (workaround) for Robolectric
A basic custom Robolectric Shadow class, `ShadowActivity`, inside `src/test/java`, provides an
example workaround. It does the following:
* In `startActivityForResult`, actually launches the next activity with the `ActivityScenario` api.
* In `finish()` (of the next activity), sends its result back to the calling activity.
* In `reset()`, clears static state. This is called at the end of each test.

This custom `ShadowActivity` is defined in `app/src/test/resources/robolectric.properties`.

## Running tests

Tests can be run in a few ways.
### On the command-line
`./gradlew testDebugUnitTest connectedDebugAndroidTest`

This produces reports in `app/build/reports/tests` (subfolders `tests` and `androidTests`)

### In Android Studio
Clicking the green triangle next to a test may not work 😢.

Instead, you can do the following:
Run -> Edit Configurations... -> Gradle
* For a robolectric test: Put the following for "Run":
    ```
    :app:testDebugUnitTest --tests "com.example.simpledemo.SharedUnitTest"
    ```
    You should be able to run the test in debug mode as well, with breakpoints.
* For an instrumentation test: Put the following for "Run":
    ```
    :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="com.example.simpledemo.SharedUnitTest"
    ```
    If you run the test in debug mode, breakpoints won't be hit. 😥 Don't dispair, there's
    a workaround (we've got lots of them here!). You can temporarily comment-out this part of
    `app/build.gradle.kts`:
    ```kotlin
    sourceSets.named("test") {
        //java.srcDirs("src/sharedTest/java")
    }
    ```
    Android Studio apparently doesn't like the same file being used by multiple source sets,
    even though it works fine on the command line. With this modification, you can run the
    test by clicking the triangle, and it will run on a connected emulator/device, with
    breakpoint debugging possible.

## Limitations
In addition to the limitations with launching tests from Andrdoid Studio, mentioned above,
a few caveats are worth mentioning.

This has only been tested on the very basic example app here.

To support launching activities "normally" (not "for result"), `startActivity` should be
implemented in a similar way.

### Why java for the `ShadowActivity`?
The shadow class is in Java, not Kotlin. This is due to an issue with the cleanup function `reset()`.
This function must be public and static. The robolectric processor looks for `@Resetter` annotated methods
which are `public static void`, on shadow classes, so it can know to invoke them at the end
of tests. Normally, this could be done in Kotlin, by defining a method annotated with `@JvmStatic`,
inside a `companion object`, and by using kapt instead of annoationProcessor. However, when doing this,
the generated Kotlin stub file doesn't have the `reset` method generated correctly: it's defined
as a non-static method inside a static inner `companion` class. It's as if `@JvmStatic` were ignored.

```java
class ShadowActivity {
    //...
    public static class companion {
        public void reset() { // not static, not on `ShadowActivity`.
            // cleanup
        }
    }
}
```

### Disclaimers
The compose code in the activities is most certainly not following best practices! 😅
The idea is to have the simplest code possible (not necessarily the most robust),
just for demonstration purposes.



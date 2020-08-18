## Privacy Policy

First and foremost, Simple Markdown DOES NOT collect any personally identifiable information. The
internet access permission is requested primarily for retrieving images from the internet in case
you embed them in your markdown, but it also allows me to send automated error and crash reports
to myself whenever the app runs into an issue. These error reports are powered by [ACRA](https://github.com/ACRA/acra), which is
an open source error reporting solution, and are sent to a private server that only I have access
to, where it is stored for up to 90 days before being permanently deleted. These error reports are
used exclusively for fixing problems that occur while you're using the app, and contain only the
bare minimum information that I need to be able to resolve the issue. The information sent may
include:

- the version of Android your device is running (e.g. 7.1)
- details about the version of SimpleMarkdown that you are using (Google Play version, Samsung Galaxy Apps version, FDroid version, version number, etc)
- any logs that SimpleMarkdown (but not other apps) might have created during execution
- the app identifier (com.wbrawner.simplemarkdown)
- your device's manufacturer (Samsung, Huawei, LG, etc)
- your device's model (Galaxy S8, Mate 10 pro, G7, etc)
- your device's configuration at app start and at the moment of the crash (the configuration tells me things like if you had the keyboard open, and if you were using the app in landscape or portrait mode. See the end of this privacy policy for a sample of that this might look like.)
- an estimation of the amount of available memory your device has (7353192448)
- an estimation of the amount of total memory your device has (55540875264)
- a stacktrace of the error (a stacktrace tells me which part of my code caused the error, what kind of error it was, and which parts of the code ran up to that error. See the end of this privacy policy for a sample of what this might look like.)
- any custom settings you have set within SimpleMarkdown (your default launch screen, default file directory, etc. Note that this does NOT include settings specific to your device, like whether or not you have WiFi enabled for example)
- details about which thread the crash occurred on (e.g. main, file handling, network)
- the time and date you started the app
- the time and date the crash occurred

This information is the only information that I collect, and it's strictly used for finding and fixing issues in the code as quickly as possible. If you don't feel comfortable with this however, you are able to opt-out from the settings menu. Should you choose to opt-out of automated crash reports, I would very much appreciate it if you could contact me when you run into an issue either by email: [support@wbrawner.com](mailto:support@wbrawner.com) or by submitting an issue via the [GitHub page](https://github.com/wbrawner/SimpleMarkdown).

### Sample data

A sample configuration:

```
locale=fr_FR
hardKeyboardHidden=HARDKEYBOARDHIDDEN_YES
keyboard=KEYBOARD_NOKEYS
keyboardHidden=KEYBOARDHIDDEN_NO
fontScale=1.0
mcc=208
mnc=10
navigation=NAVIGATION_TRACKBALL
navigationHidden=NAVIGATIONHIDDEN_NO
orientation=ORIENTATION_PORTRAIT
screenLayout=SCREENLAYOUT_SIZE_NORMAL+SCREENLAYOUT_LONG_YES
seq=117
touchscreen=TOUCHSCREEN_FINGER
uiMode=UI_MODE_TYPE_NORMAL+UI_MODE_NIGHT_NO
userSetLocale=false
```

A sample stacktrace:

```
java.io.IOException: No such file or directory
	at java.io.UnixFileSystem.createFileExclusively0(UnixFileSystem.java)
	at java.io.UnixFileSystem.createFileExclusively(UnixFileSystem.java:280)
	at java.io.File.createNewFile(File.java:948)
	at com.wbrawner.simplemarkdown.model.MarkdownFile.save(MarkdownFile.java:152)
	at com.wbrawner.simplemarkdown.presentation.MarkdownPresenterImpl.saveMarkdown(MarkdownPresenterImpl.java:120)
	at com.wbrawner.simplemarkdown.presentation.MarkdownPresenterImpl.lambda$saveMarkdown$2$MarkdownPresenterImpl(MarkdownPresenterImpl.java:120)
	at com.wbrawner.simplemarkdown.presentation.MarkdownPresenterImpl$$Lambda$2.run(MarkdownPresenterImpl.java)
	at android.os.Handler.handleCallback(Handler.java:751)
	at android.os.Handler.dispatchMessage(Handler.java:95)
	at android.os.Looper.loop(Looper.java:154)
	at android.app.ActivityThread.main(ActivityThread.java:6121)
	at java.lang.reflect.Method.invoke(Method.java)
	at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:889)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:779)
```

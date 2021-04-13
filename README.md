# [Simple Markdown](https://wbrawner.com/portfolio/simple-markdown/)

[![pipeline status](https://github.com/wbrawner/SimpleMarkdown/actions/workflows/android.yml/badge.svg)](https://github.com/wbrawner/SimpleMarkdown/actions/workflows/android.yml)

Simple Markdown is simply a Markdown editor :) I wrote it to offer up an open source alternative to
the other Markdown editors available on the Play Store. I also wanted to get some practice in
creating Android apps and have a little something to put into my portfolio.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.wbrawner.simplemarkdown.free/)

## Roadmap

* [x] Auto-save
* [x] Night mode
* [x] Save to cloud (Dropbox, Google Drive, OneDrive)
* [x] Custom CSS for Markdown preview
* [ ] Better insert for tables/images/links
* [ ] Quick-insert toolbar for common Markdown syntax characters
* [ ] Auto-scroll preview to match edit view in landscape mode
* [ ] Disable live preview in landscape mode
* [ ] Disable preview tab for better performance in large files

## Building

Using Android Studio is the preferred way to build the project. To build from the command line, you can run

    ./gradlew assembleDebug

### Crashlytics

SimpleMarkdown makes use of Firebase Crashlytics for error reporting. You'll need to follow the 
[Get started with Firebase Crashlytics](https://firebase.google.com/docs/crashlytics/get-started?platform=android) guide in order to build the project.

## Contributing

I'd love any contributions, particularly in improving the existing code. Please just fork the
repository, make your changes, squash your commits, and submit a pull request :)

## License

```
   Copyright 2017-2019 William Brawner

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

## Libraries

- [AndDown](https://github.com/commonsguy/cwac-anddown)
- [Hoedown](https://github.com/hoedown/hoedown)

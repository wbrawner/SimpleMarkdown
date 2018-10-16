# [Simple Markdown](https://wbrawner.com/portfolio/simple-markdown/)

[![Build Status](https://ci.wbrawner.com/job/Simple%20Markdown/badge/icon)](https://ci.wbrawner.com/job/Simple%20Markdown/)

Simple Markdown is simply a Markdown editor :) I wrote it to offer up an open source alternative to
the other Markdown editors available on the Play Store. I also wanted to get some practice in
creating Android apps and have a little something to put into my portfolio.

## Roadmap

* [x] Auto-save
* [ ] Night mode
* [ ] Save to cloud (Dropbox, Google Drive, OneDrive)
* [ ] Disable live preview in landscape mode
* [ ] Disable preview tab for better performance in large files
* [ ] Custom CSS for Markdown preview
* [ ] Auto-scroll preview to match edit view in landscape mode
* [ ] Better insert for tables/images/links
* [ ] Quick-insert toolbar for common Markdown syntax characters

## Building

Using Android Studio is the preferred way to build the project. To build from the command line, you can run

    ./gradlew assembleDebug

### ACRA

SimpleMarkdown makes use of [ACRA](https://github.com/ACRA/acra) for crash reports. This requires the presence of the `app/acra.properties` file for building. For your convenience, a `app/acra.properties.sample` file has been provided.

## Contributing

I'd love any contributions, particularly in improving the existing code. Please just fork the
repository, make your changes, squash your commits, and submit a pull request :)

## License

```
   Copyright 2017 William Brawner

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
- [Butterknife](https://github.com/JakeWharton/butterknife)
- [Dagger 2](https://github.com/google/dagger)
- [Hoedown](https://github.com/hoedown/hoedown)
- [Retrolambda](https://github.com/evant/gradle-retrolambda)
- [RxAndroid](https://github.com/ReactiveX/RxAndroid)
- [RxJava](https://github.com/ReactiveX/RxJava)

# WCBN Player for Android

Simple wrapper around the stream at http://wcbn.org/. Includes album art
scraping from iTunes, three quality settings, social sharing, and a
rudimentary interface for reading upcoming shows.

## Building

Requirements:
* Android Studio
* git

Import into Android Studio and build using the included APK wizard or
gradle wrapper.

If you want to redistribute, make sure you customize the gradle build
files to include your own keys instead of the default provided.

## Technical Information

The stream metadata using is parsed with StreamScraper.

## License

WCBN Player is Free Software.

It was licensed under the GNU GPLv3 until July 31, 2015, at which point
I added an additional, non-copyleft license as an option, Apache 2.0.
Please see COPYING.txt for more details and for copyright notices of
open source components used.

Code is (c) 2013-2015 Mike Huang.

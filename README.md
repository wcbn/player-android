# WCBN Player for Android

Simple wrapper around the stream at http://wcbn.org/. Includes album art
scraping from iTunes, three quality settings, social sharing, and a
rudimentary interface for reading upcoming shows.

## Building

Requirements:
* Gradle 1.6+ (or Android Studio)
* git

Either import into Android Studio and build using the included APK
wizard or use gradle to build from the command line:

gradle assemble

If you want to redistribute, make sure you customize the gradle build
files to include your own keys instead of the default provided.

Code is (c) 2013 Mike Huang.

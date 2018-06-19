# gabien-android

## Description

Android backend for gabien.

Provides graphics, sound, single-touch input and text input (no keyboard).

## Build Instructions

Good luck. I advise you NOT to use the same package name for whatever you're doing, or the same app name.

A script like the following should suffice:

    rm -rf staging
    gradle build &&
    mkdir staging &&
    cd staging &&
    unzip -o ../../gabien-common/build/libs/gabien-common.jar &&
    unzip -o ../build/libs/gabien-app-santa.jar &&
    cd ../../gabien-android &&
    ./releaser.sh SANTA claus.santa v0.1 1 ../gabien-app-santa/staging ../gabien-app-santa/icon.png android.permission.INTERNET,android.permission.WRITE_EXTERNAL_STORAGE

## License

    gabien-android - gabien backend for Android
    Written starting in 2016 by contributors (see CREDITS.txt)
    To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
    You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

## Versioning

This version of gabien-android is known to work with:
 - gabien-common 4b32a9e9e471cc21fb0c0e5ec6395033beb6a3c2
 - R48 24f31ed59978b91c341cd880b2f4a79457c9d719

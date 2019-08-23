![Joy](screenshot.png)

JoyAndroidSample
================

Setup
=====
Libraries

    mkdir app/libs
    ln -s <protolib> app/libs/protobuf-lite-3.0.1.jar

AARs

    mkdir joy-android
    ln -s <path/to/JoyAndroid/app/build/outputs/aar/app-debug.aar> joy-android/
    ln -s <path/to/JoyAndroid/app/build/outputs/aar/app-release.aar> joy-android/

Build
=====

    ./gradlew build

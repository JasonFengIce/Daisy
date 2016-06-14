#!/usr/bin/env bash
gradle build

adb install -r ./Daisy/build/outputs/apk/Daisy-release.apk

adb shell am start -n "tv.ismar.daisy/tv.ismar.daisy.ui.activity.AdvertisementActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER


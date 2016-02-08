Wear Sensors
===============

This is a utility that shows all kinds of useful information about the
sensors on your Android and Android Wear devices. When built in release mode
and installed to your phone or tablet, it will also install a wearable APK
to your Android Wear device.

Buttons are provided to cycle between all the available sensors. For each
sensor, the id, type, vendor, range, accuracy, and latency are shown. The
raw sensor values are shown in numeric form, as a bar graph, and as a plot
over time.

When used on Android Wear, the app is able to show updates even when the display
is in ambient mode.

See the screenshots below for examples of what the application looks like.



###### Phone-based Activity
<img src="screenshots/phone.png" width="270" height="480" alt="Screenshot Phone"/>

###### Wear-based Activity
<img src="screenshots/round-activity.png" width="320" height="320" alt="Screenshot Wear Activity 1"/>



Building
--------

This sample uses the Gradle build system. To build this project in release
mode with the embedded wearable APK, you will need to use
"gradlew assembleRelease" or use Android Studio and the "Generate Signed APK"
menu option.



Support
-------

- Google+ Community: https://g.co/androidweardev
- StackOverflow: https://stackoverflow.com/questions/tagged/android-wear

If you've found an error in this sample, please file an issue:
https://github.com/google/wear-sensors

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING for more
details.



License
-------

Copyright 2015 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

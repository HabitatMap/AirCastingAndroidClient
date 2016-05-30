1) Install android development tools (`brew install android-sdk` on OSX or from website).

Install all the packages for Android 2.3.1 (API v.9).

Install Extras.

2) Install maven (`brew install maven` on OSX).

3) Clone https://github.com/simpligility/android-maven-plugin

Run `mvn install` inside that repo to install all the android dependencies for maven.

If maven has problems with fetching `maps-7_r1.jar` download the package and install it manually with `mvn install:install-file -Dfile=maps-7_r1.jar -DgroupId=com.google.android.maps -DartifactId=maps -Dversion=7_r1 -Dpackaging=jar`

4) In Aircasting directory:

  `mvn install` builds the app.

  `adb install -r target/aircasting.apk` (re)installs app on your device.

  `adb shell am start -n pl.llp.aircasting/.activity.SplashActivity` starts the app from command line.

1) Install IntelliJ Idea

2) Install android development tools (brew install android-sdk or from website).

Install all the packages for Android 2.2 (API v.8).

Install Extras.

3) Install maven (`brew install maven` on OSX).

4) Clone https://github.com/simpligility/android-maven-plugin

Run mvn install -P 2.2 inside that repo so maven now knows about all the android dependencies.

If maven has problems with fetching `maps-7_r1.jar` download the package and install it manually for maven with `mvn install:install-file -Dfile=maps-7_r1.jar -DgroupId=com.google.android.maps -DartifactId=maps -Dversion=7_r1 -Dpackaging=jar`

5) In Aircasting directory:

  `mvn install` builds the app.

  `adb install -r target/aircasting.apk` (re)installs app on your device.

  `adb shell am start -n pl.llp.aircasting/.activity.SplashActivity` starts the app from command line.

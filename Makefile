release:
	mvn install -DskipTests=true -P release

adb.install:
	adb install -r target/aircasting.apk && adb shell am start -n pl.llp.aircasting/.activity.SplashActivity

install: release adb.install

test:
	mvn test

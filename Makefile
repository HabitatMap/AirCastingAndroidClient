release:
	mvn install -P release
	zipalign -v 4 target/aircasting.apk aircasting.apk

prepare:
	mvn install -DskipTests=true -P release

adb.install:
	adb install -r target/aircasting.apk && adb shell am start -n pl.llp.aircasting/.activity.SplashActivity

install: prepare adb.install

test:
	mvn test

verify:
	jarsigner -verify -certs -verbose aircasting.apk
	zipalign -c -v 4 aircasting.apk

clean:
	rm -rf target
	rm -f *.apk

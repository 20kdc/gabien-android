#!/bin/sh
# gabien-android - gabien backend for Android
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

# Just a boring releaser helper script.
# name, package, version-name, version-code, app-staging

# now for the parts that you'll probably need to adjust
# yes, these are my personal build settings -
# I may replace this build process with just injecting the classes into a gradle compile somehow
ANDROID_JAR=/home/20kdc/Documents/tools/adt-bundle-linux-x86-20140702/sdk/platforms/android-20/android.jar
ANDROID_BT=/home/20kdc/Documents/tools/adt-bundle-linux-x86-20140702/sdk/build-tools/android-4.4W

sed "s/REPLACE1/$2/;s/REPLACE2/$4/;s/REPLACE3/$3/" < AndroidManifestTemplate.xml > AndroidManifest.xml &&
echo "<resources><string name=\"app_name\">$1</string></resources>" > res/values/strings.xml &&
mkdir -p staging staging2 &&
rm -rf staging staging2 &&
mkdir -p staging staging2 &&

# Now, javac all the things
javac -source 1.6 -target 1.6 -cp "$ANDROID_JAR:$5" -d staging src/gabien/* &&
# Merge in everything, run Dx
cp -r $5/* staging/ &&
$ANDROID_BT/dx --dex --output staging2/classes.dex staging &&
$ANDROID_BT/aapt p -f -I $ANDROID_JAR -M AndroidManifest.xml -S res -A staging/assets -F result.apk &&
cd staging2 &&
$ANDROID_BT/aapt a ../result.apk classes.dex &&
# Obviously, I'll move this stuff into a config file or something if I ever release to the real Play Store - and will change my keystore
# For making debug keys that'll probably live longer than me:
# keytool -genkeypair -keyalg RSA -validity 36500
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass "android" -sigFile CERT ../result.apk mykey &&
echo "Okay"

gradle --stop
./gradlew clean --refresh-dependencies
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/wrapper/
./gradlew build

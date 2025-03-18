gradle --stop
rm -rf ~/.gradle
rm -rf ~/.m2
rm -rf .gradle
rm -rf build
rm -rf out
./gradlew clean --refresh-dependencies
JWT_SECRET=w5j4+3X7f8K9l0m1n2o3p4q5r6s7t8u9v0w1x2y3z4A5B6C7D8E9F0G1H ./gradlew build --stacktrace
java -DJWT_SECRET=w5j4+3X7f8K9l0m1n2o3p4q5r6s7t8u9v0w1x2y3z4A5B6C7D8E9F0G1H -jar ./build/libs/api-0.0.1-SNAPSHOT.jar
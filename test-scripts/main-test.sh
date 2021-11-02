set -x
cd ../run && mvn clean install && cd ../sample-main-artifact && mvn clean install && cd .. && mvn foundation.fluent.api:run:main -Dartifact=foundation.fluent.api:sample-main-artifact:1.0-SNAPSHOT -DallowSnapshot=true

set -x
cd ../run && mvn clean install && cd ../sample-testng-artifact && mvn clean install && cd .. && mvn foundation.fluent.api:run:1.1-SNAPSHOT:testng -Dartifact=foundation.fluent.api:sample-testng-artifact:1.0-SNAPSHOT -DallowSnapshot=true -X

run integration test manually:  mvn clean integration-test -Ptomcat-embedded-9
run jetty: mvn clean package jetty:run-exploded
JDK baseline JAVA-8

debug tests:  mvn clean integration-test -Ptomcat-embedded-9 -Dmaven.surefire.debug
and remote debug into port 5005

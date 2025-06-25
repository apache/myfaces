# Quarkus MyFaces Showcase

This sample application shows Quarkus Myfaces extension usage

## Running

`mvn clean package -DskipTests && java -jar ./target/quarkus-myfaces-showcase-1.0-SNAPSHOT-runner.jar`

## Dev mode

In [dev mode](https://quarkus.io/guides/maven-tooling#development-mode) you can easily debug and hot deploy your quarkus application:

`mvn quarkus:dev` 

> Hot deployment also works for facelets pages  

## Native mode

For native mode you need [graalvm 19.3.1](https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-19.3.1), see graalvm installation [instructions here](https://quarkus.io/guides/building-native-image).

> Note that for now `native mode` is not working on GraalVM **20.0.0** 

Following is how to `build and run` the native image:

`mvn clean package -Pnative && ./target/quarkus-myfaces-showcase-1.0-SNAPSHOT-runner`

## Testing 

Tests use [HTMLUnit](https://github.com/HtmlUnit/htmlunit) to retrieve web pages and assert their content.

A `mvn clean test` will run JVM mode tests

For `native mode ` tests use:

```
mvn verify -Pnative
``` 

The build-logic (equivalent to a Gradle plugin) does not seem to be required.

Only adding a `TestEngine` in the test runtime classpath would load the `TestEngine` in the test.

https://github.com/gradle/gradle/blob/v8.10.0/platforms/documentation/docs/src/docs/userguide/jvm/java_testing.adoc#filtering-test-engine

https://github.com/gradle/gradle/tree/v8.10.0/platforms/documentation/docs/src/snippets/testing/junitplatform-engine

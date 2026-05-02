plugins {
    id("java")
    alias(libs.plugins.jmh)
}

group = "com.bhf.mapish"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.guava.testlib)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.vintage.engine)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

jmh {
    includes.add(".*Benchmark.*")
    warmupIterations.set(0)
    iterations.set(1)
    fork.set(1)
    resultFormat.set("JSON")
}

plugins {
    id("java")
    id("jacoco")
    id("maven-publish")
    alias(libs.plugins.jmh)
}

group = "com.bhf.mapish"
// Version is now managed in gradle.properties

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

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

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Off-Heap Mapish")
                description.set("A high-performance, single-threaded, off-heap hash map implementation in Java")
                url.set("https://github.com/bhf/mapish")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/bhf/mapish")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.register("bumpVersion") {
    description = "Bumps the version in gradle.properties. Use -Ptype=[major|minor|patch]"
    group = "versioning"
    doLast {
        val type = project.findProperty("type") as? String ?: "patch"
        val propsFile = file("gradle.properties")
        val content = propsFile.readText()
        val versionRegex = Regex("version=([0-9]+)\\.([0-9]+)\\.([0-9]+)(.*)")
        val match = versionRegex.find(content) ?: throw GradleException("Could not find version matching X.Y.Z in gradle.properties")
        
        val (major, minor, patch, suffix) = match.destructured
        
        var newMajor = major.toInt()
        var newMinor = minor.toInt()
        var newPatch = patch.toInt()
        
        when (type.lowercase()) {
            "major" -> { newMajor++; newMinor = 0; newPatch = 0 }
            "minor" -> { newMinor++; newPatch = 0 }
            "patch" -> newPatch++
            else -> throw GradleException("Unknown release type: $type. Use major, minor, or patch.")
        }
        
        val newVersion = "$newMajor.$newMinor.$newPatch$suffix"
        val newContent = content.replace("version=${match.groupValues[1]}.${match.groupValues[2]}.${match.groupValues[3]}$suffix", "version=$newVersion")
        propsFile.writeText(newContent)
        
        println("Bumped version: ${match.groupValues[1]}.${match.groupValues[2]}.${match.groupValues[3]}$suffix -> $newVersion")
    }
}

tasks.register("release") {
    description = "Bumps version, commits, pushes, and creates a formal GitHub release to trigger CI publishing"
    group = "versioning"
    dependsOn("bumpVersion")
    
    doLast {
        val propsFile = file("gradle.properties")
        val content = propsFile.readText()
        val versionRegex = Regex("version=(.*)")
        val match = versionRegex.find(content) ?: throw GradleException("Could not find version in gradle.properties")
        val newVersion = match.groupValues[1].trim()
        val tagName = "v$newVersion"
        
        println("==> Staging gradle.properties...")
        ProcessBuilder("git", "add", "gradle.properties").redirectErrorStream(true).start().waitFor()
        
        println("==> Committing version bump to $newVersion...")
        ProcessBuilder("git", "commit", "-m", "Release $tagName").redirectErrorStream(true).start().waitFor()
        
        println("==> Pushing commit to GitHub...")
        ProcessBuilder("git", "push", "origin", "HEAD").redirectErrorStream(true).start().waitFor()
        
        println("==> Creating formal GitHub Release $tagName...")
        val ghProcess = ProcessBuilder("gh", "release", "create", tagName, "--generate-notes", "--title", "Release $tagName")
            .redirectErrorStream(true)
            .start()
        val ghOutput = ghProcess.inputStream.bufferedReader().readText()
        ghProcess.waitFor()
        if (ghProcess.exitValue() != 0) {
            println(ghOutput)
            throw GradleException("Failed to create GitHub release")
        }
        
        println("==> Successfully released $tagName! The GitHub action should now publish the packages.")
    }
}

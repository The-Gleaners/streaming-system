
tasks {
    named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        enabled = false
    }

    named<org.gradle.jvm.tasks.Jar>("jar") {
        enabled = true
    }

    named<Test>("test") {
        useJUnitPlatform()
    }
}

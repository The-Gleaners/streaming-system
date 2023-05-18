plugins {
    id("com.github.davidmc24.gradle.plugin.avro") version "1.7.1"
    id("com.github.imflog.kafka-schema-registry-gradle-plugin")
}

description = "AVRO Schema Module"

dependencies {
    implementation("org.apache.avro:avro:1.11.0")
}

avro {
    setCreateSetters(false)
}

sourceSets {
    main {
        resources {
            srcDir("src/main/java/gleaners/avro")
        }
    }
}

schemaRegistry {
    url.set("http://localhost:8085")
    quiet.set(false)

    register.subject(
            "gleaners.avro.Product",
            "avro/src/main/java/gleaners/avro/product.avsc",
            "AVRO"
    )

    register.subject(
            "gleaners.avro.DownloadTarget",
            "avro/src/main/java/gleaners/avro/download_target.avsc",
            "AVRO")

    config {
        subject("product", "FULL")
        subject("downloadTarget", "FULL")
    }
}

tasks.named("jar") {
    val register = tasks.getByName("registerSchemasTask")
    val configure = tasks.getByName("configSubjectsTask")
    val download = tasks.getByName("testSchemasTask")

    this.dependsOn(register, configure, download)

    configure.mustRunAfter(register)
    download.mustRunAfter(configure)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

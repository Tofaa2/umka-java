plugins {
    id("java")
    application
    id("io.github.krakowski.jextract") version "0.5.0"
}

group = "me.tofaa.umka"
version = "1.0-SNAPSHOT"


val umkaWindowsPathLocal = rootDir.absolutePath + "\\umka-lang\\umka_windows_mingw"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    jextract {
        header(umkaWindowsPathLocal + "\\umka_api.h") {
            targetPackage = "me.tofaa.umka.generated"
            className = "UnsafeUmka"
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview"))
}

tasks.withType<Test> {
    jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED",
        "-Djava.library.path=${umkaWindowsPathLocal}"
    )
    environment("PATH", "${umkaWindowsPathLocal};${System.getenv("PATH")}")
}



tasks.withType<JavaExec> {
    environment("PATH", "${umkaWindowsPathLocal};${System.getenv("PATH")}")
    jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED",
        "-Djava.library.path=${umkaWindowsPathLocal}"
    )
}


sourceSets {
    main {
        resources {
            setSrcDirs(listOf("src/main/resources"))
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

application {
    mainClass.set("me.tofaa.umka.Main")
    applicationDefaultJvmArgs = listOf("--enable-preview", "--enable-native-access=ALL-UNNAMED")
}
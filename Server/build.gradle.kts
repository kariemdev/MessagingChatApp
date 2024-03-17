
plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}
configurations {
    all {
        exclude(group = "commons-logging", module = "commons-logging")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.mongodb:mongodb-driver-sync:4.4.2")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation ("org.slf4j:slf4j-api:1.7.30")
    implementation ("org.slf4j:jul-to-slf4j:1.7.30")
    implementation ("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.ultreia:bluecove:2.1.1")
    implementation ("org.subethamail:subethasmtp:3.1.7")
    testImplementation("org.mockito:mockito-core:3.+")
    testImplementation ("org.powermock:powermock-module-junit4:2.0.2")
    testImplementation ("org.powermock:powermock-api-mockito2:2.0.2")


}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("chatapp.Server")
}



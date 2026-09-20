plugins {
    id("java-library")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

}
sourceSets {
    getByName("main") {
        java {
            srcDirs("src/main/java", "../app/src/main/java")
            //
            include("com/github/tand0/andshogio/tool/**")
            include("com/github/tand0/andshogio/util/**")
        }
    }
}
dependencies {
    implementation(libs.annotations)

    // Source: https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation(libs.sqlite.jdbc)
    testImplementation(libs.junit)
}
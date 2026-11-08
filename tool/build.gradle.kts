plugins {
    java
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
            include("com/github/tand0/shogio/engin/**")
            include("com/github/tand0/shogio/util/**")
            include("com/github/tand0/shogio/tool/**")
            include("com/github/tand0/shogio/linux/**")
        }
    }
}
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.4.0")
    implementation(libs.annotations)
    implementation(libs.tensorflow.core.platform)
    // Source: https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation(libs.sqlite.jdbc)
    testImplementation(libs.junit)


    // 7z (LZMA) の圧縮・解凍をサポートするために必須のライブラリ
    implementation(libs.commons.compress)
    implementation(libs.xz)
}

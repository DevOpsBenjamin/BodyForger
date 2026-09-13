plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

// The upload keystore, resolved once for every module that signs a release.
//
// ⚠️ The Wear Data Layer only pairs two apps that share an applicationId AND a
// signing certificate. app-mobile and app-wear must therefore sign with the same
// key, or the watch stops talking to the phone the day the first release ships.
val uploadKeystore = java.util.Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

extra["uploadKeystoreFile"] = rootProject.file(
    uploadKeystore.getProperty("KEYSTORE_FILE")
        ?: (findProperty("storeFile") as? String)
        ?: "bodyforger-upload-key.jks"
)
extra["uploadKeystorePassword"] = uploadKeystore.getProperty("KEYSTORE_PASSWORD")
    ?: (findProperty("storePassword") as? String)
    ?: System.getenv("BODYFORGER_STORE_PASSWORD")
extra["uploadKeyAlias"] = uploadKeystore.getProperty("KEY_ALIAS")
    ?: (findProperty("keyAlias") as? String)
    ?: "bodyforger-upload"
extra["uploadKeyPassword"] = uploadKeystore.getProperty("KEY_PASSWORD")
    ?: (findProperty("keyPassword") as? String)
    ?: System.getenv("BODYFORGER_KEY_PASSWORD")

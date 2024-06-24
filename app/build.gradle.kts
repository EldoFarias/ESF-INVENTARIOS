plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.farias.inventario"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.farias.inventario"
        minSdk = 24
        targetSdk = 34
        versionCode = 31
        versionName = "1.31"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    //IMPLEMENTANDO A BIBLIOTECA DE CÓDIGOS DE BARRA
    implementation ("com.google.zxing:core:3.4.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.2.0")

    //IMPLEMENTAÇÃO BIBLIOTECA DE PERMISSÕES
    implementation("com.vmadalin:easypermissions-ktx:1.0.0")

    //IMPLEMENTANDO A BIBLIOTECA DE PDF
    implementation("com.itextpdf:itext7-core:7.1.15")

    //IMPLEMENTANDO A CRIPOGRAFIA
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    //IMPLEMENTANDO A CORROTINA
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.activity:activity:1.9.0")

    // IMPLEMENTANDO A PARTE DE PAGAMENTOS
    implementation("com.android.billingclient:billing-ktx:6.2.1")

    //NÃO ESTÁ NA DOCUMENTAÇÃO MAS PRECISA ADICIONAR ESTA BIBLIOTECA PARA
    //FUNCIONAR CORRETAMENTE A PARTE DE PAGAMENTOS DO BILLING
    implementation("com.google.guava:guava:27.0.1-android")
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))

    //Firebase autenticação
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    //Firebase firestore
    implementation("com.google.firebase:firebase-firestore-ktx:24.11.1")
    //Firebase storage
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")

    //Dependencia do RETROFIT
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.leanback:leanback:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
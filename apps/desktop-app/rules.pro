# Global settings to avoid aggressive transformations that break some Kotlin/Ktor bytecode
-dontoptimize
-dontobfuscate
-ignorewarnings

# Keep important attributes for Kotlin/Compose/reflection
-keepattributes Signature, InnerClasses, EnclosingMethod,
    RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations,
    RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations,
    PermittedSubclasses, NestHost, NestMembers, Record

# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-keep class io.ktor.client.engine.cio.** { *; }

-keep class org.slf4j.** { *; }

-keep class coil3.** { *; }

-keep class ai.koog.** { *; }

-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**
-dontnote io.ktor.**
-dontnote org.slf4j.**
-dontnote kotlinx.serialization.**

# Compose Desktop / Skiko / LWJGL
-dontwarn org.jetbrains.skiko.**
-dontwarn org.jetbrains.skia.**
-dontwarn org.lwjgl.**
-dontwarn androidx.compose.**
-dontwarn org.jetbrains.compose.resources.**

# Coroutines/Serialization/IO
-dontwarn kotlinx.coroutines.**
-dontwarn kotlinx.io.**
-dontwarn okio.**

# Dependency Injection / Logging / Utils
-dontwarn org.kodein.**
-dontwarn io.github.oshai.**
-dontwarn org.intellij.lang.annotations.**
-dontwarn javax.annotation.**

# Media/Rendering/Markdown/Images
-dontwarn coil3.**
-dontwarn com.mikepenz.**

# Project library ecosystem
-dontwarn ai.koog.**
-dontwarn io.modelcontextprotocol.**
-dontwarn com.xemantic.ai.**
-dontwarn com.eygraber.**
-dontwarn com.ashampoo.kim.**

# Broad suppressions for transitive libs not used at runtime on desktop
-dontwarn io.lettuce.**
-dontwarn io.grpc.**
-dontwarn io.opentelemetry.**
-dontwarn io.micrometer.**
-dontwarn reactor.**
-dontwarn rx.**
-dontwarn io.reactivex.**
-dontwarn io.reactivex.rxjava3.**
-dontwarn org.apache.commons.pool2.**
-dontwarn com.google.auto.value.**
-dontwarn com.google.common.**
-dontwarn com.fasterxml.jackson.**
-dontwarn okhttp3.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.graalvm.**
-dontwarn com.oracle.svm.**
-dontwarn org.openjsse.**
-dontwarn android.**
-dontwarn sun.misc.**
-dontwarn javax.enterprise.**

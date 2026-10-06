# kotlinx.serialization keeps the generated serializers of the content models.
-keepclassmembers class app.qurandua.shared.model.** {
    *** Companion;
}
-keepclasseswithmembers class app.qurandua.shared.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class app.qurandua.shared.**$$serializer { *; }

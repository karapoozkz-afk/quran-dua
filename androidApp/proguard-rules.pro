# kotlinx.serialization keeps the generated serializers of the content models.
-keepclassmembers class app.bibliya.shared.model.** {
    *** Companion;
}
-keepclasseswithmembers class app.bibliya.shared.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class app.bibliya.shared.**$$serializer { *; }

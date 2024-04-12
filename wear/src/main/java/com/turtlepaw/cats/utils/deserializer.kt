import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class AnimalPhoto(
    val id: String,
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
    val gifUrl: String? = null,
    val posterUrl: String? = null
)

// Custom deserializer for handling different API responses
@Serializer(forClass = AnimalPhoto::class)
object AnimalPhotoSerializer : KSerializer<AnimalPhoto> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnimalPhoto")

    override fun serialize(encoder: Encoder, value: AnimalPhoto) {
        // Custom serialization logic, if needed
    }

    override fun deserialize(decoder: Decoder): AnimalPhoto {
        val element = decoder.decodeSerializableValue(JsonObject.serializer())
        return if (element.containsKey("media")) {
            // Deserialization logic for bunnies
            val id = element["id"]!!.jsonPrimitive.content
            val media = element["media"]!!.jsonObject
            val gifUrl = media["gif"]!!.jsonPrimitive.content
            val posterUrl = media["poster"]!!.jsonPrimitive.content

            AnimalPhoto(
                id = id,
                url = posterUrl,
                gifUrl = gifUrl,
                posterUrl = posterUrl
            )
        } else {
            // Deserialization logic for cats
            Json.decodeFromJsonElement(AnimalPhoto.serializer(), element)
        }
    }
}

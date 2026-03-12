package collabcanvas

import com.google.gson.*
import java.lang.reflect.Type

class CanvasWebSocketMessageAdapter : JsonDeserializer<CanvasWebSocketMessage> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CanvasWebSocketMessage {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type").asString
        return when (type) {
            "ELEMENT_ADDED" -> context.deserialize(json, ElementAddedMessage::class.java)
            "ELEMENT_UPDATED" -> context.deserialize(json, ElementUpdatedMessage::class.java)
            "ELEMENT_REMOVED" -> context.deserialize(json, ElementRemovedMessage::class.java)
            else -> throw JsonParseException("Unknown message type: $type")
        }
    }
}

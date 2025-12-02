import api.parse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import serialization.toJson
import kotlin.js.JsExport

private val json = Json {
    prettyPrint = true
    @OptIn(ExperimentalSerializationApi::class)
    prettyPrintIndent = "  "
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun toJSON(classFile: ByteArray): String {
    val cf = parse(classFile)
    val jsonObject = cf.toJson()
    return json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), jsonObject)
}

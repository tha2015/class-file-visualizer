expect fun getPlatformName(): String

fun greet(): String {
    return "Hello from ${getPlatformName()}!"
}

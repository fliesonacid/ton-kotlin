package org.ton.adnl

fun ipv4(ipv4: Int): String = buildString {
    append(ipv4 and 0xFF)
    append(".")
    append((ipv4 shr 8) and 0xFF)
    append(".")
    append((ipv4 shr 16) and 0xFF)
    append(".")
    append((ipv4 shr 24) and 0xFF)
}

fun ipv4(host: String): Int {
    val bytes = host.split('.')
    require(bytes.size == 4) { "Invalid IPv4 address" }
    var result = 0
    bytes.forEach { byte ->
        result = (result shl 8) or (byte.toInt() and 0xFF)
    }
    return result
}

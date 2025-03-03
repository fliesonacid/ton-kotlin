package org.ton.lite.api.liteserver

import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
import org.ton.crypto.Base64ByteArraySerializer
import org.ton.crypto.base64
import org.ton.tl.TlConstructor
import org.ton.tl.constructors.readBytesTl
import org.ton.tl.constructors.writeBytesTl

@Serializable
data class LiteServerSendMessage(
    @Serializable(Base64ByteArraySerializer::class)
    val body: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LiteServerSendMessage

        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        return body.contentHashCode()
    }

    override fun toString(): String = buildString {
        append("LiteServerSendMessage(body=")
        append(base64(body))
        append(")")
    }

    companion object : TlConstructor<LiteServerSendMessage>(
        type = LiteServerSendMessage::class,
        schema = "liteServer.sendMessage body:bytes = liteServer.SendMsgStatus"
    ) {
        override fun decode(input: Input): LiteServerSendMessage {
            val body = input.readBytesTl()
            return LiteServerSendMessage(body)
        }

        override fun encode(output: Output, value: LiteServerSendMessage) {
            output.writeBytesTl(value.body)
        }
    }
}
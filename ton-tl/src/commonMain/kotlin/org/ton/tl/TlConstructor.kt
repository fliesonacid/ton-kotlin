package org.ton.tl

import io.ktor.utils.io.core.*
import org.intellij.lang.annotations.Language
import org.ton.crypto.crc32
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

abstract class TlConstructor<T : Any>(
    val type: KType,
    @Language("TL")
    val schema: String
) : TlCodec<T> {
    constructor(type: KClass<T>, schema: String) : this(type.createType(), schema)

    val id: Int = crc32(schema)

    fun calculatePadding(size: Int): Int = (size % 4).let { if (it > 0) 4 - it else 0 }

    fun encodeBoxed(message: T): ByteArray = buildPacket {
        encodeBoxed(this, message)
        val padding = calculatePadding(size)
        repeat(padding) {
            writeByte(0)
        }
    }.readBytes()

    override fun encodeBoxed(output: Output, value: T) {
        output.writeIntLittleEndian(id)
        encode(output, value)
    }

    fun <R : Any> Output.writeBoxedTl(message: R, codec: TlConstructor<R>) = codec.encodeBoxed(this, message)

    override fun decodeBoxed(input: Input): T {
        val actualId = input.readIntLittleEndian()
        require(actualId == id) { "Invalid ID. expected: $id actual: $actualId" }
        return decode(input)
    }

    fun <R : Any> Input.readBoxedTl(codec: TlConstructor<R>) = codec.decodeBoxed(this)

    override fun toString(): String = schema
}


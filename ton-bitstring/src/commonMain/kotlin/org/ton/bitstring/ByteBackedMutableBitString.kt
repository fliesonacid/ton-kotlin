package org.ton.bitstring

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

open class ByteBackedMutableBitString(
    override var bytes: ByteArray,
    override var size: Int
) : ByteBackedBitString(size, bytes), MutableBitString {
    override operator fun set(index: Int, bit: Int) {
        set(index, bit != 0)
    }

    override operator fun set(index: Int, element: Boolean): Boolean {
        val newBytes = expandByteArray(bytes, index + 1)
        val previous = set(newBytes, index, element)
        bytes = newBytes
        return previous
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableBitString =
        slice(fromIndex..toIndex).toMutableBitString()

    override fun plus(bits: BooleanArray): BitString = plus(bits.asIterable())
    override fun plus(bytes: ByteArray): ByteBackedMutableBitString = append(bytes, bytes.size * Byte.SIZE_BITS)
    override fun plus(bits: Iterable<Boolean>): ByteBackedMutableBitString = apply {
        if (bits is ByteBackedBitString) {
            append(bits.bytes, bits.size)
        } else {
            val bitsCollection = if (bits is Collection<Boolean>) bits else bits.toList()
            val bitsCount = bitsCollection.size

            val newBytes = expandByteArray(bytes, size + bitsCount)
            bitsCollection.forEachIndexed { index, bit ->
                set(newBytes, size + index, bit)
            }
            bytes = newBytes
            size += bitsCount
        }
    }

    override fun iterator(): MutableIterator<Boolean> = listIterator()
    override fun listIterator(): MutableListIterator<Boolean> = BitStringMutableIterator(this)
    override fun listIterator(index: Int): MutableListIterator<Boolean> = BitStringMutableIterator(this, index)

    override fun add(element: Boolean): Boolean {
        val newBytes = expandByteArray(bytes, size + 1)
        set(newBytes, size + 1, element)
        bytes = newBytes
        size += 1
        return true
    }

    override fun add(index: Int, element: Boolean) {
        TODO("Not yet implemented")
    }

    override fun addAll(index: Int, elements: Collection<Boolean>): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<Boolean>): Boolean {
        plus(elements)
        return true
    }

    override fun clear() {
        bytes = ByteArray(0)
        size = 0
    }

    override fun remove(element: Boolean): Boolean = removeAt(lastIndexOf(element))

    override fun removeAll(elements: Collection<Boolean>): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAt(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<Boolean>): Boolean {
        TODO("Not yet implemented")
    }

    private fun append(bytes: ByteArray, bits: Int) = apply {
        checkSize(size + bits)
        if (bits != 0) {
            if (size % 8 == 0) {
                if (bits % 8 == 0) {
                    appendWithoutShifting(bytes, bits)
                } else {
                    appendWithShifting(bytes, bits)
                }
            } else {
                appendWithDoubleShifting(bytes, bits)
            }
        }
    }

    private fun appendWithoutShifting(byteArray: ByteArray, bits: Int) {
        require(size % 8 == 0)
        require(bits % 8 == 0)

        val newBytes = expandByteArray(bytes, size + bits)
        byteArray.copyInto(
            destination = newBytes,
            destinationOffset = size / Byte.SIZE_BITS,
            endIndex = bits / Byte.SIZE_BITS
        )
        bytes = newBytes
        size += bits
    }

    private fun appendWithShifting(byteArray: ByteArray, bits: Int) {
        require(size % 8 == 0)
        val shift = bits % 8
        require(shift != 0)

        val newBytes = expandByteArray(bytes, size + bits)
        byteArray.copyInto(
            destination = newBytes,
            destinationOffset = size / Byte.SIZE_BITS,
            endIndex = bits / Byte.SIZE_BITS + 1
        )
        var lastByte = byteArray[bits / Byte.SIZE_BITS].toInt()
        lastByte = lastByte shr (8 - shift)
        lastByte = lastByte shl (8 - shift)
        newBytes[(size + bits) / Byte.SIZE_BITS] = lastByte.toByte()
        bytes = newBytes
        size += bits
    }

    private fun appendWithDoubleShifting(byteArray: ByteArray, bits: Int) {
        val selfShift = size % 8
        val data = bytes.copyOf(size / 8 + byteArray.size + 1)
        val lastIndex = size / 8
        val lastBits = data[lastIndex].toInt() shr (8 - selfShift)
        var y = lastBits
        byteArray.forEachIndexed { i, x ->
            y = (y shl 8) or (x.toInt() and 0xFF)
            val newByte = y shr selfShift
            data[lastIndex + i] = newByte.toByte()
        }
        val a = lastIndex + byteArray.size
        data[a] = (y shl (8 - selfShift)).toByte()

        val newSize = size + bits
        val shift = newSize % 8
        if (shift == 0) {
            val newBytes = expandByteArray(data, newSize)
            bytes = newBytes
            size = newSize
        } else {
            val newBytes = expandByteArray(data, newSize)
            var lastByte = newBytes[newBytes.lastIndex].toInt()
            lastByte = lastByte shr (8 - shift)
            lastByte = lastByte shl (8 - shift)
            newBytes[newBytes.lastIndex] = lastByte.toByte()
            bytes = newBytes
            size = newSize
        }
    }

    class BitStringMutableIterator(
        override val bitString: MutableBitString,
        index: Int = 0
    ) : BitStringIterator(bitString, index), MutableListIterator<Boolean> {
        override fun add(element: Boolean) {
            bitString.add(index, element)
        }

        override fun remove() {
            bitString.removeAt(index)
        }

        override fun set(element: Boolean) {
            bitString[index] = element
        }
    }

    companion object {
        @JvmStatic
        fun of(size: Int = 0): ByteBackedMutableBitString {
            val bytes = constructByteArray(size)
            return ByteBackedMutableBitString(bytes, size)
        }

        @JvmStatic
        fun of(byteArray: ByteArray, size: Int = byteArray.size * Byte.SIZE_BITS): ByteBackedMutableBitString {
            val bytes = constructByteArray(byteArray, size)
            return ByteBackedMutableBitString(bytes, size)
        }

        @JvmStatic
        fun of(bitString: BitString, size: Int = bitString.size): ByteBackedMutableBitString {
            return if (bitString is ByteBackedBitString) {
                of(bitString.bytes, size)
            } else {
                val result = of(size)
                bitString.forEachIndexed { index, bit ->
                    result[index] = bit
                }
                result
            }
        }

        @JvmStatic
        protected fun set(bytes: ByteArray, index: Int, element: Boolean): Boolean {
            val wordIndex = index.byteIndex
            val bitMask = index.bitMask
            val previous = get(bytes, index)
            if (element) {
                bytes[wordIndex] = bytes[wordIndex] or bitMask
            } else {
                bytes[wordIndex] = bytes[wordIndex] and bitMask.inv()
            }
            return previous
        }
    }
}

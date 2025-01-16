package org.tabooproject.modlink.binary

import io.netty.buffer.ByteBuf
import org.tabooproject.modlink.PacketAssembler
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.function.Supplier

/**
 * 二进制读取器类，用于从字节数组中读取各种数据类型。
 *
 * @param bytes 要读取的字节数组。
 */
class BinaryReader(bytes: ByteArray) {

    /** 
     * 用于读取数据的 ByteBuffer 实例。
     * 使用大端字节序（Big-Endian）进行数据读取。
     */
    val buffer: ByteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)

    /**
     * 读取一个字符串。
     *
     * @return 读取到的字符串。
     */
    fun readString(): String {
        val size = buffer.int
        val bytes = ByteArray(size)
        buffer.get(bytes, 0, size)
        return String(bytes, StandardCharsets.UTF_8)
    }

    /**
     * 读取一个可为空的字符串。
     *
     * @return 读取到的字符串，如果为 null 则返回 null。
     */
    fun readNullableString(): String? {
        val size = buffer.int
        return if (size == -1) null else {
            val bytes = ByteArray(size)
            buffer.get(bytes, 0, size)
            String(bytes, StandardCharsets.UTF_8)
        }
    }

    /**
     * 读取一个字节。
     *
     * @return 读取到的字节。
     */
    fun readByte(): Byte = buffer.get()

    /**
     * 读取一个短整型。
     *
     * @return 读取到的短整型。
     */
    fun readShort(): Short = buffer.short

    /**
     * 读取一个整型。
     *
     * @return 读取到的整型。
     */
    fun readInt(): Int = buffer.int

    /**
     * 读取一个长整型。
     *
     * @return 读取到的长整型。
     */
    fun readLong(): Long = buffer.long

    /**
     * 读取一个浮点型。
     *
     * @return 读取到的浮点型。
     */
    fun readFloat(): Float = buffer.float

    /**
     * 读取一个双精度浮点型。
     *
     * @return 读取到的双精度浮点型。
     */
    fun readDouble(): Double = buffer.double

    /**
     * 读取一个字符。
     *
     * @return 读取到的字符。
     */
    fun readChar(): Char = buffer.char

    /**
     * 读取一个布尔值。
     *
     * @return 读取到的布尔值。
     */
    fun readBoolean(): Boolean = buffer.get() != 0.toByte()

    /**
     * 读取一个列表。
     *
     * @param factory 用于创建列表元素的工厂函数。
     * @return 读取到的列表。
     */
    inline fun <reified T> readList(factory: () -> T): MutableList<T> {
        val size = buffer.int
        return (0 until size).mapTo(ArrayList()) { factory() }
    }

    /**
     * 读取一个数组。
     *
     * @param factory 用于创建数组元素的工厂函数。
     * @return 读取到的数组。
     */
    inline fun <reified T> readArray(factory: () -> T): Array<T> {
        val size = buffer.int
        return Array(size) { factory() }
    }

    /**
     * 读取一个可为空的对象。
     *
     * @param factor 用于创建对象的供应者。
     * @return 读取到的对象，如果为 null 则返回 null。
     */
    fun <T> readNullableObj(factor: Supplier<T>): T? {
        val size = buffer.int
        return if (size == -1) null else factor.get()
    }

    /**
     * 读取一个属性映射。属性中的值只能是 SerializationType 类型。
     *
     * @throws IllegalStateException 当遇到不支持的类型时抛出。
     */
    fun readProperties(): Map<String, Any> {
        val size = buffer.int
        return (0 until size).associate {
            val key = readNullableString()!!
            val value = BinarySerializer.readFrom(this)
            key to value
        }
    }

    /**
     * 读取字节数组
     */
    fun readByteArray(): ByteArray {
        val size = buffer.int
        val bytes = ByteArray(size)
        buffer.get(bytes, 0, size)
        return bytes
    }

    companion object {

        /**
         * 从 ByteBuf 创建一个 BinaryReader 实例。
         *
         * @param buffer 要读取的 ByteBuf。
         * @return 创建的 BinaryReader 实例。
         */
        fun from(buffer: ByteBuf): BinaryReader = BinaryReader(buffer.array())

        /**
         * 从字节数组创建一个 BinaryReader 实例。
         *
         * @param bytes 要读取的字节数组。
         * @return 创建的 BinaryReader 实例。
         */
        fun from(bytes: ByteArray): BinaryReader = BinaryReader(bytes)
    }
}
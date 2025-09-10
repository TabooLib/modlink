package org.tabooproject.modlink.binary

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.function.Consumer

/**
 * 二进制写入器
 * 基于 ByteArrayOutputStream 实现自动扩容
 */
class BinaryWriter {

    /** 用于自动扩容的字节数组输出流 */
    val byteArrayOutputStream = ByteArrayOutputStream()
    
    /** 数据输出流，用于写入基本数据类型 */
    val output = DataOutputStream(byteArrayOutputStream)

    /**
     * 写入一个字符串。
     *
     * @param str 要写入的字符串。
     */
    fun writeString(str: String) {
        val bytes = str.encodeToByteArray()
        output.writeInt(bytes.size)
        output.write(bytes)
    }

    /**
     * 写入一个可为空的字符串。
     *
     * @param str 要写入的字符串，可以为 null。
     */
    fun writeNullableString(str: String?) {
        if (str == null) {
            output.writeInt(-1)
        } else {
            val bytes = str.toByteArray()
            output.writeInt(bytes.size)
            output.write(bytes)
        }
    }

    /**
     * 写入一个整数。
     *
     * @param value 要写入的整数值。
     */
    fun writeInt(value: Int) {
        output.writeInt(value)
    }

    /**
     * 写入一个长整数
     *
     * @param value 要写入的长整数值。
     */
    fun writeLong(value: Long) {
        output.writeLong(value)
    }

    /**
     * 写入一个浮点数。
     *
     * @param value 要写入的浮点数值。
     */
    fun writeFloat(value: Float) {
        output.writeFloat(value)
    }

    /**
     * 写入一个浮点数（双精度）。
     *
     * @param value 要写入的浮点数值。
     */
    fun writeDouble(value: Double) {
        output.writeDouble(value)
    }

    /**
     * 写入一个布尔值。
     *
     * @param value 要写入的布尔值。
     */
    fun writeBoolean(value: Boolean) {
        output.writeBoolean(value)
    }

    /**
     * 写入一个字符
     *
     * @param value 要写入的字符
     */
    fun writeChar(value: Char) {
        output.writeChar(value.code)
    }

    /**
     * 写入一个可序列化对象列表。
     *
     * @param list 要写入的 BinarySerializable 对象列表。
     */
    fun writeList(list: List<BinarySerializable>) {
        output.writeInt(list.size)
        list.forEach { it.writeTo(this) }
    }

    /**
     * 写入一个泛型列表，使用自定义写入器。
     *
     * @param list 要写入的列表。
     * @param writer 用于写入列表元素的消费者函数。
     */
    fun <T> writeList(list: List<T>, writer: Consumer<T>) {
        output.writeInt(list.size)
        list.forEach { writer.accept(it) }
    }

    /**
     * 写入一个可序列化对象。
     *
     * @param obj 要写入的 BinarySerializable 对象。
     */
    fun writeObj(obj: BinarySerializable) {
        obj.writeTo(this)
    }

    /**
     * 写入一个可为空的可序列化对象。
     *
     * @param obj 要写入的 BinarySerializable 对象，可以为 null。
     */
    fun writeNullableObj(obj: BinarySerializable?) {
        if (obj == null) {
            output.writeInt(-1)
        } else {
            output.writeInt(0)
            obj.writeTo(this)
        }
    }

    /**
     * 写入一个属性映射。属性中的值只能是 SerializationType 类型。
     *
     * @param properties 要写入的属性映射。
     * @throws IllegalStateException 当遇到不支持的类型时抛出。
     */
    fun writeProperties(properties: Map<String, Any>) {
        output.writeInt(properties.size)
        properties.forEach { (key, value) ->
            writeNullableString(key)
            BinarySerializer.writeTo(output, value)
        }
    }

    /**
     * 写入字节数组
     */
    fun writeByteArray(bytes: ByteArray) {
        output.writeInt(bytes.size)
        output.write(bytes)
    }

    /**
     * 获取写入的字节数组。
     *
     * @return 包含所有写入数据的字节数组。
     */
    fun toByteArray(): ByteArray {
        return byteArrayOutputStream.toByteArray()
    }
}
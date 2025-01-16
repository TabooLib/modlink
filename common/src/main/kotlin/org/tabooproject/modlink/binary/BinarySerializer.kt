package org.tabooproject.modlink.binary

import org.tabooproject.modlink.Reflection
import java.io.DataOutputStream

object BinarySerializer {

    // 类型映射表
    private val typeMapping = hashMapOf<Class<*>, SerializationType>().apply {
        // Kotlin 类型
        put(Byte::class.java, SerializationType.BYTE)
        put(Short::class.java, SerializationType.SHORT)
        put(Int::class.java, SerializationType.INT)
        put(Long::class.java, SerializationType.LONG)
        put(Float::class.java, SerializationType.FLOAT)
        put(Double::class.java, SerializationType.DOUBLE)
        put(Boolean::class.java, SerializationType.BOOLEAN)
        put(Char::class.java, SerializationType.CHAR)

        // Java 类型
        put(java.lang.Byte::class.java, SerializationType.BYTE)
        put(java.lang.Short::class.java, SerializationType.SHORT)
        put(Integer::class.java, SerializationType.INT)
        put(java.lang.Long::class.java, SerializationType.LONG)
        put(java.lang.Float::class.java, SerializationType.FLOAT)
        put(java.lang.Double::class.java, SerializationType.DOUBLE)
        put(java.lang.Boolean::class.java, SerializationType.BOOLEAN)
        put(Character::class.java, SerializationType.CHAR)

        // 其他类型保持不变
        put(String::class.java, SerializationType.STRING)

        // 基础类型数组
        put(ByteArray::class.java, SerializationType.BYTE_ARRAY)
        put(ShortArray::class.java, SerializationType.SHORT_ARRAY)
        put(IntArray::class.java, SerializationType.INT_ARRAY)
        put(LongArray::class.java, SerializationType.LONG_ARRAY)
        put(FloatArray::class.java, SerializationType.FLOAT_ARRAY)
        put(DoubleArray::class.java, SerializationType.DOUBLE_ARRAY)
        put(BooleanArray::class.java, SerializationType.BOOLEAN_ARRAY)
        put(CharArray::class.java, SerializationType.CHAR_ARRAY)
    }

    // 获取类型的辅助方法
    fun getSerializationType(value: Any): SerializationType {
        return when (value) {
            is Array<*> -> SerializationType.ARRAY
            is List<*> -> SerializationType.LIST
            is Map<*, *> -> SerializationType.MAP
            else -> {
                val clazz = Reflection.getReferenceType(value.javaClass)
                typeMapping[clazz] ?: error("Unsupported type: $clazz")
            }
        }
    }

    fun writeTo(output: DataOutputStream, value: Any) {
        val type = getSerializationType(value)
        output.writeInt(type.id)
        when (type) {
            SerializationType.BYTE -> output.writeByte((value as Byte).toInt())
            SerializationType.SHORT -> output.writeShort((value as Short).toInt())
            SerializationType.INT -> output.writeInt(value as Int)
            SerializationType.LONG -> output.writeLong(value as Long)
            SerializationType.FLOAT -> output.writeFloat(value as Float)
            SerializationType.DOUBLE -> output.writeDouble(value as Double)
            SerializationType.BOOLEAN -> output.writeBoolean(value as Boolean)
            SerializationType.CHAR -> output.writeChar((value as Char).code)
            SerializationType.STRING -> {
                val str = value as String
                output.writeInt(str.length)
                output.write(str.toByteArray(), 0, str.length)
            }

            SerializationType.ARRAY -> {
                val array = value as Array<*>
                output.writeInt(array.size)
                array.forEach { writeTo(output, it!!) }
            }

            SerializationType.LIST -> {
                val list = value as List<*>
                output.writeInt(list.size)
                list.forEach { writeTo(output, it!!) }
            }

            SerializationType.MAP -> {
                val map = value as Map<*, *>
                output.writeInt(map.size)
                map.forEach { (k, v) ->
                    writeTo(output, k!!)
                    writeTo(output, v!!)
                }
            }

            SerializationType.BYTE_ARRAY -> {
                val array = value as ByteArray
                output.writeInt(array.size)
                output.write(array)
            }

            SerializationType.SHORT_ARRAY -> {
                val array = value as ShortArray
                output.writeInt(array.size)
                array.forEach { output.writeShort(it.toInt()) }
            }

            SerializationType.INT_ARRAY -> {
                val array = value as IntArray
                output.writeInt(array.size)
                array.forEach { output.writeInt(it) }
            }

            SerializationType.LONG_ARRAY -> {
                val array = value as LongArray
                output.writeInt(array.size)
                array.forEach { output.writeLong(it) }
            }

            SerializationType.FLOAT_ARRAY -> {
                val array = value as FloatArray
                output.writeInt(array.size)
                array.forEach { output.writeFloat(it) }
            }

            SerializationType.DOUBLE_ARRAY -> {
                val array = value as DoubleArray
                output.writeInt(array.size)
                array.forEach { output.writeDouble(it) }
            }

            SerializationType.BOOLEAN_ARRAY -> {
                val array = value as BooleanArray
                output.writeInt(array.size)
                array.forEach { output.writeBoolean(it) }
            }

            SerializationType.CHAR_ARRAY -> {
                val array = value as CharArray
                output.writeInt(array.size)
                array.forEach { output.writeChar(it.code) }
            }
        }
    }

    fun readFrom(reader: BinaryReader): Any {
        val typeId = reader.readInt()
        val type = SerializationType.Companion.fromId(typeId)
        return when (type) {
            SerializationType.BYTE -> reader.readByte()
            SerializationType.SHORT -> reader.readShort()
            SerializationType.INT -> reader.readInt()
            SerializationType.LONG -> reader.readLong()
            SerializationType.FLOAT -> reader.readFloat()
            SerializationType.DOUBLE -> reader.readDouble()
            SerializationType.BOOLEAN -> reader.readBoolean()
            SerializationType.CHAR -> reader.readChar()
            SerializationType.STRING -> reader.readString()
            SerializationType.ARRAY -> reader.readArray { readFrom(reader) }
            SerializationType.LIST -> reader.readList { readFrom(reader) }
            SerializationType.MAP -> {
                val size = reader.readInt()
                HashMap<Any, Any>(size).apply {
                    repeat(size) {
                        val key = readFrom(reader)
                        val value = readFrom(reader)
                        put(key, value)
                    }
                }
            }

            SerializationType.BYTE_ARRAY -> {
                val size = reader.readInt()
                ByteArray(size) { reader.readByte() }
            }

            SerializationType.SHORT_ARRAY -> {
                val size = reader.readInt()
                ShortArray(size) { reader.readShort() }
            }

            SerializationType.INT_ARRAY -> {
                val size = reader.readInt()
                IntArray(size) { reader.readInt() }
            }

            SerializationType.LONG_ARRAY -> {
                val size = reader.readInt()
                LongArray(size) { reader.readLong() }
            }

            SerializationType.FLOAT_ARRAY -> {
                val size = reader.readInt()
                FloatArray(size) { reader.readFloat() }
            }

            SerializationType.DOUBLE_ARRAY -> {
                val size = reader.readInt()
                DoubleArray(size) { reader.readDouble() }
            }

            SerializationType.BOOLEAN_ARRAY -> {
                val size = reader.readInt()
                BooleanArray(size) { reader.readBoolean() }
            }

            SerializationType.CHAR_ARRAY -> {
                val size = reader.readInt()
                CharArray(size) { reader.readChar() }
            }
        }
    }
}
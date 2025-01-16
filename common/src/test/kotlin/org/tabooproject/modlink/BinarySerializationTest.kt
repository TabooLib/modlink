package org.tabooproject.modlink

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.tabooproject.modlink.binary.BinaryReader
import org.tabooproject.modlink.binary.BinarySerializable
import org.tabooproject.modlink.binary.BinaryWriter

class BinarySerializationTest {

    // 测试基本的读写操作
    @Test
    fun testBasicReadWrite() {
        val writer = BinaryWriter()
        
        // 写入基本数据类型
        writer.writeString("Hello World")
        writer.writeNullableString("Nullable String")
        writer.writeNullableString(null)
        writer.writeInt(42)
        writer.writeBoolean(true)

        // 获取写入的字节数组
        val bytes = writer.toByteArray()
        
        // 创建读取器
        val reader = BinaryReader(bytes)
        
        // 验证读取的数据
        assertEquals("Hello World", reader.readString())
        assertEquals("Nullable String", reader.readNullableString())
        assertNull(reader.readNullableString())
        assertEquals(42, reader.readInt())
        assertTrue(reader.readBoolean())
    }

    // 测试复杂对象的序列化
    @Test
    fun testComplexObjectSerialization() {
        // 创建一个测试用的可序列化对象
        class TestObject : BinarySerializable {
            var name: String = "test"
            var value: Int = 100

            override fun writeTo(writer: BinaryWriter) {
                writer.writeString(name)
                writer.writeInt(value)
            }
        }

        val writer = BinaryWriter()
        val testObj = TestObject()
        writer.writeObj(testObj)

        val bytes = writer.toByteArray()
        val reader = BinaryReader(bytes)

        // 读取并验证对象数据
        assertEquals("test", reader.readString())
        assertEquals(100, reader.readInt())
    }

    // 测试列表的序列化
    @Test
    fun testListSerialization() {
        val writer = BinaryWriter()
        val list = listOf("one", "two", "three")
        
        writer.writeList(list) { str -> writer.writeString(str) }
        
        val bytes = writer.toByteArray()
        val reader = BinaryReader(bytes)
        
        val size = reader.readInt()
        assertEquals(3, size)
        
        repeat(size) { i ->
            assertEquals(list[i], reader.readString())
        }
    }

    // 测试 Reflection 工具类
    @Test
    fun testReflection() {
        // 测试原始类型转换
        assertEquals(Integer::class.java, Reflection.getReferenceType(Integer.TYPE))
        assertEquals(java.lang.Boolean::class.java, Reflection.getReferenceType(java.lang.Boolean.TYPE))
        assertEquals(java.lang.Double::class.java, Reflection.getReferenceType(java.lang.Double.TYPE))

        // 测试类型兼容性检查
        val left = arrayOf(Number::class.java, CharSequence::class.java)
        val right: Array<Class<*>?> = arrayOf(Integer::class.java, String::class.java)
        assertTrue(Reflection.isAssignableFrom(left, right))

        // 测试原始类型描述符
        assertEquals(Integer.TYPE, Reflection.getPrimitiveType('I'))
        assertEquals(java.lang.Boolean.TYPE, Reflection.getPrimitiveType('Z'))
        assertEquals(java.lang.Double.TYPE, Reflection.getPrimitiveType('D'))
    }

    // 测试边界情况
    @Test
    fun testEdgeCases() {
        val writer = BinaryWriter()
        
        // 测试空字符串
        writer.writeString("")
        writer.writeNullableString("")
        
        // 测试大数值
        writer.writeInt(Int.MAX_VALUE)
        writer.writeInt(Int.MIN_VALUE)
        
        val bytes = writer.toByteArray()
        val reader = BinaryReader(bytes)
        
        assertEquals("", reader.readString())
        assertEquals("", reader.readNullableString())
        assertEquals(Int.MAX_VALUE, reader.readInt())
        assertEquals(Int.MIN_VALUE, reader.readInt())
    }

    // 测试异常情况
    @Test
    fun testExceptionalCases() {
        assertThrows(IllegalArgumentException::class.java) {
            Reflection.getPrimitiveType('X') // 无效的类型描述符
        }
    }

    // 测试 Properties 的序列化
    @Test
    fun testPropertiesSerialization() {
        val writer = BinaryWriter()
        
        // 创建一个包含各种类型的属性映射
        val properties = mapOf(
            "stringValue" to "Hello World",
            "intValue" to 42,
            "booleanValue" to true,
            "doubleValue" to 3.14,
            "floatValue" to 1.5f,
            "longValue" to 123456789L,
            "charValue" to 'A'
        )
        
        // 写入属性
        writer.writeProperties(properties)
        
        val bytes = writer.toByteArray()
        val reader = BinaryReader(bytes)

        // 使用 readProperties 读取属性
        val readProperties = reader.readProperties()
        
        // 验证所有属性值
        assertEquals("Hello World", readProperties["stringValue"])
        assertEquals(42, readProperties["intValue"])
        assertEquals(true, readProperties["booleanValue"])
        assertEquals(3.14, readProperties["doubleValue"])
        assertEquals(1.5f, readProperties["floatValue"])
        assertEquals(123456789L, readProperties["longValue"])
        assertEquals('A', readProperties["charValue"])
    }

    // 测试 Map 的边界情况
    @Test
    fun testMapEdgeCases() {
        val writer = BinaryWriter()
        
        // 测试空 Map
        writer.writeProperties(emptyMap())
        
        // 测试包含空字符串的 Map
        writer.writeProperties(mapOf(
            "emptyString" to "",
            "zeroInt" to 0,
            "emptyChar" to '\u0000'
        ))
        
        val bytes = writer.toByteArray()
        val reader = BinaryReader(bytes)
        
        // 验证空 Map
        val emptyMapProperties = reader.readProperties()
        assertTrue(emptyMapProperties.isEmpty())
        
        // 验证包含空值的 Map
        val readProperties = reader.readProperties()
        
        assertEquals("", readProperties["emptyString"])
        assertEquals(0, readProperties["zeroInt"])
        assertEquals('\u0000', readProperties["emptyChar"])
    }

    // 测试字节数组的读写
    @Test
    fun testByteArrayReadWrite() {
        val writer = BinaryWriter()
        
        // 创建一个字节数组
        val byteArray = byteArrayOf(1, 2, 3, 4, 5)
        
        // 写入字节数组
        writer.writeByteArray(byteArray)
        
        val bytes = writer.toByteArray()
        val reader = BinaryReader(bytes)
        
        // 读取字节数组
        val readByteArray = reader.readByteArray()
        
        // 验证字节数组内容
        assertArrayEquals(byteArray, readByteArray)
    }
}
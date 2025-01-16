package org.tabooproject.modlink

import io.netty.buffer.Unpooled
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.tabooproject.modlink.binary.BinaryReader
import org.tabooproject.modlink.binary.BinaryWriter
import org.tabooproject.modlink.protocol.Packet
import org.tabooproject.modlink.protocol.PacketBody

class ByteBufConversionTest {

    // 测试 MLPacket 的 ByteBuf 转换
    @Test
    fun testMLPacketByteBufConversion() {
        val id = 1
        val data = ByteArray(100) { it.toByte() }
        val packet = PacketBody(id, 0, data)

        // 转换为 ByteBuf
        val byteBuf = packet.toByteBuf()

        // 验证 ByteBuf 内容
        val reader = BinaryReader.from(byteBuf)
        assertEquals(Packet.SIGNATURE, reader.readInt()) // complex packet signature
        assertEquals(id, reader.readInt())
        assertEquals(1, reader.readInt()) // type
        assertEquals(0, reader.readInt()) // index
        assertArrayEquals(data, reader.readByteArray())
    }

    // 测试 BinaryReader 的 ByteBuf 转换
    @Test
    fun testBinaryReaderByteBufConversion() {
        val writer = BinaryWriter()
        writer.writeString("Hello ByteBuf")
        writer.writeInt(12345)

        // 转换为 ByteBuf
        val byteBuf = Unpooled.wrappedBuffer(writer.toByteArray())

        // 使用 ByteBuf 创建 BinaryReader
        val reader = BinaryReader.from(byteBuf)

        // 验证读取的数据
        assertEquals("Hello ByteBuf", reader.readString())
        assertEquals(12345, reader.readInt())
    }
} 
package org.tabooproject.modlink

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.tabooproject.modlink.protocol.Packet
import org.tabooproject.modlink.protocol.PacketHeader
import org.tabooproject.modlink.protocol.PacketBody

class PacketTest {

    // 测试 MLPacket 的分片功能
    @Test
    fun testCreatePackets() {
        val id = 1
        val dataSize = Packet.defaultChunkSize * 2 + 100 // 超过两个分片大小
        val data = ByteArray(dataSize) { it.toByte() }

        // 创建分片数据包
        val packets = Packet.chunk(id, data)

        // 验证总包数量（分片数量 + 1 个头包）
        assertEquals(4, packets.size)

        // 验证第一个包是头包
        val header = packets[0] as PacketHeader
        assertEquals(id, header.packetId)
        assertEquals(3, header.chunks)

        // 验证后续包是数据包
        val reconstructedData = ByteArray(dataSize)
        var position = 0
        for (i in 1 until packets.size) {
            val body = packets[i] as PacketBody
            assertEquals(id, body.packetId)
            assertEquals(i - 1, body.index)
            val expectedSize = if (i < packets.size - 1) Packet.defaultChunkSize else 100
            assertEquals(expectedSize, body.data.size)

            // 组装数据
            System.arraycopy(body.data, 0, reconstructedData, position, body.data.size)
            position += body.data.size
        }

        // 验证组装后的数据与原始数据相同
        assertArrayEquals(data, reconstructedData)
    }
} 
package org.tabooproject.modlink.protocol

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.tabooproject.modlink.ModLinkPacket
import org.tabooproject.modlink.binary.BinarySerializable
import org.tabooproject.modlink.binary.BinaryWriter

/**
 * 表示一个 modlink 数据包
 */
abstract class Packet(val packetId: Int) : BinarySerializable {

    fun toByteArray(): ByteArray {
        val writer = BinaryWriter()
        writer.writeInt(SIGNATURE)
        writer.writeInt(packetId)
        writer.writeObj(this)
        return writer.toByteArray()
    }

    fun toByteBuf(): ByteBuf {
        return Unpooled.buffer().apply { writeBytes(toByteArray()) }
    }

    companion object {

        // Complex Packet
        const val SIGNATURE = 1122

        /**
         * 默认分片大小（32000 字节，即 32 KB）
         * 他妈的核心都支持 1MB，你 Bukkit 还限制 32766？
         */
        var defaultChunkSize = 32000

        /**
         * 创建分片数据包
         */
        fun chunk(id: Int, bytes: ByteArray): List<Packet> {
            val chunks = (bytes.size + defaultChunkSize - 1) / defaultChunkSize
            val packets = mutableListOf<Packet>()
            packets += PacketHeader(id, bytes.size, chunks)
            for (i in 0 until chunks) {
                val start = i * defaultChunkSize
                val end = minOf(start + defaultChunkSize, bytes.size)
                val chunkData = bytes.copyOfRange(start, end)
                packets.add(PacketBody(id, i, chunkData))
            }
            return packets
        }

        /**
         * 创建分片数据包
         */
        fun chunk(id: Int, packet: ModLinkPacket): List<Packet> {
            return chunk(id, packet.toByteArray())
        }
    }
}
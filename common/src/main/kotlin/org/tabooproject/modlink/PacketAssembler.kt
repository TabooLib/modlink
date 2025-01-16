package org.tabooproject.modlink

import org.tabooproject.modlink.protocol.Packet
import org.tabooproject.modlink.protocol.PacketBody
import org.tabooproject.modlink.protocol.PacketHeader
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * 数据包重组管理器
 * 支持并行处理和乱序接收
 */
class PacketAssembler {

    // 存储正在处理中的数据包
    val assemblingPackets = ConcurrentHashMap<Int, AssemblingPacket>()

    /**
     * 处理数据包
     * @return 如果这个数据包已经完成则返回完整的数据包字节数组，否则返回 null
     */
    fun accept(packet: Packet): ByteArray? {
        val assemblingPacket = assemblingPackets.getOrPut(packet.packetId) { AssemblingPacket() }
        val result = when (packet) {
            is PacketBody -> assemblingPacket.acceptBody(packet)
            is PacketHeader -> {
                assemblingPacket.header = packet
                assemblingPacket.tryGetResult()
            }
        }
        if (result != null) {
            assemblingPackets.remove(packet.packetId)
        }
        return result
    }

    /**
     * 清理超时的数据包
     * @param timeout 超时时间（毫秒）
     */
    fun cleanup(timeout: Long = 10000) {
        val now = System.currentTimeMillis()
        assemblingPackets.entries.removeIf { (_, packet) ->
            now - packet.lastUpdateTime > timeout
        }
    }

    /**
     * 内部类：正在组装中的数据包
     */
    class AssemblingPacket {

        var header: PacketHeader? = null
        val chunks = ConcurrentHashMap<Int, ByteArray>()
        var lastUpdateTime = System.currentTimeMillis()
            private set

        /**
         * 接收数据包体
         * @return 如果数据包完成则返回完整的字节数组，否则返回 null
         */
        fun acceptBody(body: PacketBody): ByteArray? {
            lastUpdateTime = System.currentTimeMillis()
            chunks[body.index] = body.data
            return tryGetResult()
        }

        /**
         * 尝试获取完整的数据包
         * @return 如果数据包完成则返回完整的字节数组，否则返回 null
         */
        fun tryGetResult(): ByteArray? {
            val header = this.header ?: return null
            // 检查是否收到所有分片
            if (chunks.size != header.chunks) {
                return null
            }
            // 按顺序组装数据包
            return ByteArrayOutputStream(header.totalSize).use { buffer ->
                for (i in 0 until header.chunks) {
                    chunks[i]?.let { buffer.write(it) } ?: return null
                }
                buffer.toByteArray()
            }
        }
    }
}
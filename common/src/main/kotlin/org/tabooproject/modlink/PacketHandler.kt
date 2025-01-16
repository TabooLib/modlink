package org.tabooproject.modlink

import org.tabooproject.modlink.binary.BinaryReader
import org.tabooproject.modlink.protocol.Packet
import org.tabooproject.modlink.protocol.PacketBody
import org.tabooproject.modlink.protocol.PacketHeader
import java.util.concurrent.atomic.AtomicInteger

/**
 * 数据包处理器抽象类
 */
abstract class PacketHandler {

    /**
     * 数据包 ID 生成器
     */
    val id = AtomicInteger(0)

    /**
     * 获取下一个数据包 ID
     *
     * @return 数据包 ID
     */
    fun nextId(): Int {
        return id.getAndIncrement()
    }

    /**
     * 发送数据包
     * 
     * @param id 数据包 ID
     * @param packet 数据包对象
     * @param sender 发送函数
     */
    fun sendPacket(packet: ModLinkPacket, sender: (bytes: ByteArray) -> Unit) {
        val bytes = packet.toByteArray()
        if (bytes.size > Packet.defaultChunkSize) {
            Packet.chunk(nextId(), bytes).forEach { sender(it.toByteArray()) }
        } else {
            sender(bytes)
        }
    }

    /**
     * 接收并处理数据包
     * 
     * @param bytes 字节数组
     * @param assembler 数据包组装器
     * @param receiver 处理回调函数
     */
    fun receivePacket(bytes: ByteArray, assembler: PacketAssembler, receiver: (typeId: Int, reader: BinaryReader) -> Unit) {
        val reader = BinaryReader.from(bytes)
        // 获取 signature
        val signature = reader.readInt()
        if (signature == Packet.SIGNATURE) {
            val packetId = reader.readInt()
            val packet = when (val typeId = reader.readInt()) {
                0 -> PacketHeader(packetId, reader.readInt(), reader.readInt())
                1 -> PacketBody(packetId, reader.readInt(), reader.readByteArray())
                else -> error("Unknown packet type: $typeId")
            }
            val result = assembler.accept(packet)
            if (result != null) {
                val source = BinaryReader.from(result)
                source.readInt() // signature
                receiver(source.readInt(), source)
            }
        } else if (signature == ModLinkPacket.SIGNATURE) {
            receiver(reader.readInt(), reader)
        }
    }
}
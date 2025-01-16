package org.tabooproject.modlink

import org.tabooproject.modlink.binary.BinaryReader
import org.tabooproject.modlink.ModLinkPacket
import java.util.concurrent.ConcurrentHashMap

/**
 * 数据包编解码注册表
 * 用于管理数据包的编码器和解码器，实现数据包的序列化和反序列化
 */
object PacketCodecRegistry {

    /**
     * 存储数据包类型 ID 与对应的解码器函数的映射关系
     * 解码器函数负责将二进制数据转换回对应的数据包对象
     */
    val decoderMap = ConcurrentHashMap<Int, BinaryReader.() -> ModLinkPacket>()

    /**
     * 注册一个数据包类型的解码器函数
     *
     * @param typeId 数据包类型 ID
     * @param decoder 解码器函数，用于从 [BinaryReader] 中读取二进制数据并构造 [ModLinkPacket]
     */
    fun registerDecoder(typeId: Int, decoder: BinaryReader.() -> ModLinkPacket) {
        decoderMap[typeId] = decoder
    }

    /**
     * 根据类型 ID 解码数据包
     *
     * @param typeId 数据包类型 ID
     * @param reader 二进制读取器
     * @return 解码后的数据包对象
     * @throws IllegalStateException 当找不到对应的解码器函数时抛出
     */
    fun decode(typeId: Int, reader: BinaryReader): ModLinkPacket {
        val decoder = decoderMap[typeId] ?: error("Unknown packet type id: $typeId")
        return decoder(reader)
    }
}
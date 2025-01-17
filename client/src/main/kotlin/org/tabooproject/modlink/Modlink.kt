package org.tabooproject.modlink

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Modlink : PacketHandler() {

    /** 数据包重组器，用于处理分片数据包的重组 */
    val assembler = PacketAssembler()

    /** 数据包编解码器注册表，用于管理不同类型数据包的序列化和反序列化 */
    val codecRegistry = PacketCodecRegistry

    /**
     * 数据包接收回调列表
     */
    val receiveCallback = ArrayList<(packet: ModLinkPacket) -> Unit>()

    init {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            assembler.cleanup()
        }, 10, 10, TimeUnit.SECONDS)
    }

    /**
     * 添加数据包接收监听器
     * 只接收指定类型的数据包
     *
     * @param listener 监听器函数
     */
    inline fun <reified T : ModLinkPacket> onReceive(crossinline listener: (packet: T) -> Unit) {
        receiveCallback += { packet -> if (packet is T) listener(packet) }
    }

    fun handleMessageReceived(bytes: ByteArray) {
        try {
            receivePacket(bytes, assembler) { typeId, reader ->
                val packet = codecRegistry.decode(typeId, reader)
                receiveCallback.forEach { callback -> callback(packet) }
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }
}
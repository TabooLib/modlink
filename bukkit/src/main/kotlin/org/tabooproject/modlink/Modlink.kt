package org.tabooproject.modlink

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.PluginMessageListener
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.plusAssign

class Modlink(val plugin: Plugin) : PacketHandler(), PluginMessageListener, Listener {

    /** 数据包重组器，用于处理分片数据包的重组 */
    val assemblerMap = ConcurrentHashMap<String, PacketAssembler>()

    /** 数据包编解码器注册表，用于管理不同类型数据包的序列化和反序列化 */
    val codecRegistry = PacketCodecRegistry

    /**
     * 数据包接收回调列表
     */
    val receiveCallback = ArrayList<(player: Player, packet: ModLinkPacket) -> Unit>()

    init {
        Bukkit.getServer().pluginManager.registerEvents(this, plugin)
        Bukkit.getServer().messenger.registerOutgoingPluginChannel(plugin, "modlink:default")
        Bukkit.getServer().messenger.registerIncomingPluginChannel(plugin, "modlink:default", this)
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            assemblerMap.forEach { it.value.cleanup() }
        }, 100, 100)
    }

    override fun onPluginMessageReceived(channel: String, player: Player, bytes: ByteArray) {
        val assembler = assemblerMap.getOrPut(player.name) { PacketAssembler() }
        try {
            receivePacket(bytes, assembler) { typeId, reader ->
                val packet = codecRegistry.decode(typeId, reader)
                receiveCallback.forEach { callback -> callback(player, packet) }
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }

    /**
     * 添加数据包接收监听器
     *
     * @param listener 监听器函数
     */
    fun onReceive(listener: (player: Player, packet: ModLinkPacket) -> Unit) {
        receiveCallback += listener
    }

    /**
     * 添加数据包接收监听器
     * 只接收指定类型的数据包
     *
     * @param listener 监听器函数
     */
    inline fun <reified T : ModLinkPacket> onReceive(crossinline listener: (player: Player, packet: T) -> Unit) {
        onReceive { player, packet -> if (packet is T) listener(player, packet) }
    }

    /**
     * 向玩家发送数据包
     *
     * @param player 玩家对象
     * @param packet 数据包
     */
    fun sendPacket(player: Player, packet: ModLinkPacket) {
        sendPacket(packet) { sendPluginMessage(player, it) }
    }

    private fun sendPluginMessage(player: Player, bytes: ByteArray) {
        player.sendPluginMessage(plugin, "modlink:out", bytes);
    }

    @EventHandler
    private fun onQuit(e: PlayerQuitEvent) {
        assemblerMap.remove(e.player.uniqueId.toString())
    }
}
# Modlink

在 Bukkit 和 Fabric/Forge 之间进行更简单的数据传输，且允许数据包大小超过 Minecraft 规定的 1 MB。

## 基本

定义数据包:

```kotlin
data class MyPacket(val who: Int, val data: String) : ModLinkPacket(0) {

    override fun write(writer: BinaryWriter) {
        writer.writeInt(who)
        writer.writeString(data)
    }
}
```

注册数据包：

```kotlin
val modlink: Modlink = ...

modlink.codecRegistry.registerDecoder(0) {
    MyPacket(readInt(), readString())
}
```

### Bukkit

创建 Modlink 实例（使用 TabooLib 环境）:

```kotlin
val modlink by lazy { Modlink(bukkitPlugin) }
```

发送数据包：

```kotlin
modlink.sendPacket(player, MyPacket(1, "Hello, World!"))
```

接收数据包：

```kotlin
modlink.onReceive<MyPacket> { player, packet ->
    println("${player.name} received packet: $packet")
}
```

### Fabric

创建 Modlink 实例（使用 Fabric 1.20.4 环境）:

```kotlin
val modlink = Modlink()
// 注册数据包通道
// 由于 Fabric 的通讯写法在不同版本可能存在差异，因此需要手动注册
val channel = Identifier("modlink", "default")
ClientPlayNetworking.registerGlobalReceiver(channel) { _, _, buf, _ ->
    modlink.handleMessageReceived(buf.array())
}
```

发送数据包：

```kotlin
modlink.sendPacket(MyPacket(1, "Hello, World!")) { bytes ->
    // 为什么会设计成这种形式？
    // 如果数据包的大小超过 512 KB，则会被分片发送
    ClientPlayNetworking.send(channel, PacketByteBuf(Unpooled.wrappedBuffer(bytes)))
}
```

接收数据包：
```kotlin
modlink.onReceive<MyPacket> { packet ->
    println("Received packet: $packet")
}
``` 

### Forge

和 Fabric 一样，需要手动实现发送和接受函数。

## 注意事项

在 Channel 没有注册前是不能发包给玩家的，通常表现在 Join 事件前后发包无效。下面是一个简单的解决办法：

```kotlin
private val modlink by lazy { Modlink(bukkitPlugin) }
private val packetBuffer = ConcurrentHashMap<String, MutableList<ModLinkPacket>>()

/**
 * 向指定玩家发送 ModLink 数据包
 *
 * @param player 目标玩家
 * @param packet 要发送的数据包
 */
fun sendPacket(player: Player, packet: ModLinkPacket) {
    // 玩家是否已注册通道
    if (player.hasMeta("modlink")) {
        modlink.sendPacket(player, packet)
    } else {
        packetBuffer.getOrPut(player.name) { mutableListOf() } += packet
    }
}

@SubscribeEvent
private fun onJoin(e: PlayerJoinEvent) {
    e.player.removeMeta("modlink")
}

@SubscribeEvent
private fun onChannel(e: PlayerRegisterChannelEvent) {
    if (e.channel == Modlink.channelId) {
        e.player.setMeta("modlink", true)
        // 发送缓存中的数据包
        packetBuffer.remove(e.player.name)?.forEach { modlink.sendPacket(e.player, it) }
    }
}
```

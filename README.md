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
val modlink by lazy { Modlink(bukkitPlugin, "modlink:default") }
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

对于1.12.2-1.13+ 请参考[海螺的博客](https://izzel.io/2017/08/28/minecraft-plugin-message/)

对于1.16.5-1.20.1，一种方法是使用Mixin手动接管数据包处理

下面以1.20.1 Forge 47.4.3 为例
#### Mixin
```java
@Mixin({ClientPacketListener.class})
public class ClientPacketListenerMixin {
    @Inject(
            method = {"handleCustomPayload"},
            at = {@At("HEAD")},
            cancellable = true
    )
    public void onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo callbackInfo) {
        if (packet.getIdentifier().equals(ResourceLocation.tryBuild("modlink", "default"))) {
            CustomPacketEvent event = new CustomPacketEvent(packet.getData());
            MinecraftForge.EVENT_BUS.post(event);
            callbackInfo.cancel();
        }
    }
}
```
#### Forge端
创建事件类
```kotlin
class CustomPacketEvent(val buf: FriendlyByteBuf): Event()
```
之后实现收发信逻辑
```kotlin
    // 此事件需要在Forge的EVENT_BUS上注册
    fun onCustomPacketReceive(event: CustomPacketEvent) {
        Minecraft.getInstance().execute {
            val buf = event.buf
            val readableBytes = buf.readableBytes()
            if (readableBytes <= 0) {
                return@execute
            }
            val data = ByteArray(readableBytes)
            buf.readBytes(data)
            modlink.handleMessageReceived(data)
        }
    }

    fun sendToServer(packet: ModLinkPacket) {
        modlink.sendPacket(packet) { bytes ->
            Minecraft.getInstance().connection?.send(
                ServerboundCustomPayloadPacket(
                    ResourceLocation.tryBuild("modlink", "default"),
                    FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))
                )
            )
        }
    }
```
### 关于其他版本

已知在1.20.4时，使用上面的方法Mixin的类要换为`ClientCommonPacketListenerImpl`

同时由于Forge在此版本又出现了[#5730](https://github.com/MinecraftForge/MinecraftForge/issues/5730)中的问题，需要在Bukkit端手动注册通道
```kotlin
    // 使用Taboolib
    @SubscribeEvent
    fun onPlayerJoin(event: PlayerJoinEvent) {
        (event.player as CraftPlayer).addChannel("modlink:default")
    }
```

之后便可正常通过`CustomPacketEvent`收到数据包的数据
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

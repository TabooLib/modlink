# Modlink

在 Bukkit 和 Fabric/Forge 之间进行数据传输。

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

modlink.codecRegistry.registerDecoder(0) { reader ->
    MyPacket(reader.readInt(), reader.readString())
}
```

## Bukkit

创建 Modlink 实例（使用 TabooLib 环境）:

```kotlin
val modlink = Modlink(bukkitPlugin)
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

## Fabric

创建 Modlink 实例（使用 Fabric 1.20.4 环境）:

```kotlin
val modlink = Modlink()
// 注册数据包通道
// 由于 Fabric 的通讯写法在不同版本可能存在差异，因此需要手动注册
val channel = Identifier("modlink", "default")
ServerPlayNetworking.registerGlobalReceiver(channel) { _, _, _, buf, _ ->
    modlink.handleMessageReceived(buf.array())
}
```

发送数据包：

```kotlin
modlink.sendPacket(MyPacket(1, "Hello, World!")) { bytes ->
    // 为什么会设计成这种形式？
    // 如果数据包的大小超过 512 KB，则会被分片发送
    ServerPlayNetworking.send(player, channel, bytes)
}
```

接收数据包：

```kotlin
modlink.onReceive<MyPacket> { packet ->
    println("Received packet: $packet")
}
``` 

## Forge

和 Fabric 一样，需要手动实现发送和接受函数。
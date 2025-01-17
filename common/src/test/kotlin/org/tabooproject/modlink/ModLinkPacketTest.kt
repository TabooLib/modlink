package org.tabooproject.modlink

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.tabooproject.modlink.binary.BinaryReader
import org.tabooproject.modlink.binary.BinaryWriter
import org.tabooproject.modlink.protocol.Packet

class ModLinkPacketTest {

    @Test
    fun test() {
        val assembler = PacketAssembler()
        val chunked = Packet.chunk(0, PacketKeepAlive("我是傻逼"))
        var result: ByteArray? = null
        chunked.forEach { result = assembler.accept(it) }
        val reader = BinaryReader.from(result!!)
        reader.readInt() // Signature
        reader.readInt() // Type ID
        val packet = PacketKeepAlive.from(reader)
        assertEquals("我是傻逼", packet.name)
    }
}
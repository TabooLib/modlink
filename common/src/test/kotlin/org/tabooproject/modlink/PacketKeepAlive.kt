package org.tabooproject.modlink

import org.tabooproject.modlink.binary.*

data class PacketKeepAlive(val name: String) : ModLinkPacket(0) {

    override fun writeTo(writer: BinaryWriter) {
        writer.writeString(name)
    }

    companion object {

        fun from(reader: BinaryReader): PacketKeepAlive {
            return PacketKeepAlive(reader.readString())
        }
    }
}
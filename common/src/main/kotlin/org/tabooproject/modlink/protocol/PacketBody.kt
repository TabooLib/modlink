package org.tabooproject.modlink.protocol

import org.tabooproject.modlink.binary.BinaryWriter

class PacketBody(
    id: Int,
    val index: Int,
    val data: ByteArray
) : Packet(id) {

    override fun writeTo(writer: BinaryWriter) {
        writer.writeInt(1) // body
        writer.writeInt(index)
        writer.writeByteArray(data)
    }
}
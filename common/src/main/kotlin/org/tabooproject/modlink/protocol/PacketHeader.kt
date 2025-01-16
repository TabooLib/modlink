package org.tabooproject.modlink.protocol

import org.tabooproject.modlink.binary.BinaryWriter

class PacketHeader(
    id: Int,
    val totalSize: Int,
    val chunks: Int
) : Packet(id) {

    override fun writeTo(writer: BinaryWriter) {
        writer.writeInt(0) // header
        writer.writeInt(totalSize)
        writer.writeInt(chunks)
    }
}
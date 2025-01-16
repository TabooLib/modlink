package org.tabooproject.modlink

import org.tabooproject.modlink.binary.BinarySerializable
import org.tabooproject.modlink.binary.BinaryWriter

abstract class ModLinkPacket(val typeId: Int) : BinarySerializable {

    fun toByteArray(): ByteArray {
        val writer = BinaryWriter()
        writer.writeInt(SIGNATURE)
        writer.writeInt(typeId)
        writer.writeObj(this)
        return writer.toByteArray()
    }

    companion object {

        // Primitive Packet
        const val SIGNATURE = 1121
    }
}
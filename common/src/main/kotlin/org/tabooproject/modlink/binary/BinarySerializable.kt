package org.tabooproject.modlink.binary

interface BinarySerializable {

    /**
     * 写入到 ByteBuffer
     */
    fun writeTo(writer: BinaryWriter)
}
package org.tabooproject.modlink

import java.lang.Byte
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.lang.Short

/**
 * 反射工具类，提供类型转换和判断的工具方法。
 */
object Reflection {

    /**
     * 判断两个类型数组是否可以相互赋值。
     *
     * @param left 左侧类型数组。
     * @param right 右侧类型数组，可以包含 null。
     * @return 如果右侧数组中的类型都可以赋值给左侧对应位置的类型，返回 true；否则返回 false。
     */
    fun isAssignableFrom(left: Array<Class<*>>, right: Array<Class<*>?>): Boolean {
        if (left.size != right.size) {
            return false
        }
        return left.indices.all { right[it] == null || getReferenceType(left[it]).isAssignableFrom(getReferenceType(right[it]!!)) }
    }

    /**
     * 根据描述符获取原始类型。
     *
     * @param descriptor 类型描述符字符。
     * @return 对应的原始类型 Class 对象。
     * @throws IllegalArgumentException 当描述符无效时抛出。
     */
    fun getPrimitiveType(descriptor: Char): Class<*> {
        return when (descriptor) {
            'B' -> Byte.TYPE
            'C' -> Character.TYPE
            'D' -> Double.TYPE
            'F' -> Float.TYPE
            'I' -> Integer.TYPE
            'J' -> Long.TYPE
            'S' -> Short.TYPE
            'V' -> Void.TYPE
            'Z' -> java.lang.Boolean.TYPE
            else -> throw IllegalArgumentException()
        }
    }

    /**
     * 获取原始类型对应的引用类型。
     *
     * @param primitive 原始类型的 Class 对象。
     * @return 对应的引用类型 Class 对象。如果输入不是原始类型，则返回原输入。
     */
    fun getReferenceType(primitive: Class<*>): Class<*> {
        return when (primitive) {
            Integer.TYPE -> Integer::class.java
            Character.TYPE -> Character::class.java
            Byte.TYPE -> Byte::class.java
            Long.TYPE -> Long::class.java
            Double.TYPE -> Double::class.java
            Float.TYPE -> Float::class.java
            Short.TYPE -> Short::class.java
            java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
            else -> primitive
        }
    }
}
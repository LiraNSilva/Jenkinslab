/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.instantexecution.serialization.codecs

import org.gradle.instantexecution.extensions.uncheckedCast

import org.gradle.instantexecution.serialization.Codec
import org.gradle.instantexecution.serialization.DecodingProvider
import org.gradle.instantexecution.serialization.EncodingProvider
import org.gradle.instantexecution.serialization.ReadContext
import org.gradle.instantexecution.serialization.SerializerCodec
import org.gradle.instantexecution.serialization.WriteContext

import org.gradle.internal.serialize.Serializer

import kotlin.reflect.KClass


class BindingsBackedCodec(private val bindings: List<Binding>) : Codec<Any?> {

    internal
    companion object {

        operator fun invoke(bindings: BindingsBuilder.() -> Unit) =
            BindingsBackedCodec(
                BindingsBuilder().apply(bindings).build()
            )

        private
        const val NULL_VALUE: Byte = -1
    }

    private
    val encodings = HashMap<Class<*>, Encoding>()

    override suspend fun WriteContext.encode(value: Any?) = when (value) {
        null -> writeByte(NULL_VALUE)
        else -> taggedEncodingFor(value.javaClass).run {
            encode(value)
        }
    }

    override suspend fun ReadContext.decode() = when (val tag = readByte()) {
        NULL_VALUE -> null
        else -> bindings[tag.toInt()].decoding.run { decode() }
    }

    private
    fun taggedEncodingFor(type: Class<*>): Encoding =
        encodings.computeIfAbsent(type, ::computeEncoding)

    private
    fun computeEncoding(type: Class<*>): Encoding {
        for (binding in bindings) {
            val encoding = binding.encodingForType(type)
            if (encoding != null) {
                return TaggedEncoding(binding.tag, encoding)
            }
        }
        throw IllegalArgumentException("Don't know how to serialize an object of type ${type.name}.")
    }

    private
    class TaggedEncoding(
        private val tag: Byte,
        private val encoding: Encoding
    ) : Encoding {
        override suspend fun WriteContext.encode(value: Any) {
            writeByte(tag)
            encoding.run { encode(value) }
        }
    }
}


data class Binding(
    val tag: Byte,
    val encodingForType: EncodingProducer,
    val decoding: Decoding
)


typealias EncodingProducer = (Class<*>) -> Encoding?


typealias Encoding = EncodingProvider<Any>


typealias Decoding = DecodingProvider<Any>


internal
class BindingsBuilder {

    private
    val bindings = mutableListOf<Binding>()

    fun build(): List<Binding> = bindings.toList()

    fun bind(type: Class<*>, codec: Codec<*>) {
        require(bindings.all { it.encodingForType(type) == null })
        val codecForAny = codec.uncheckedCast<Codec<Any>>()
        val encodingProducer = encodingForSubTypesOf(type, codecForAny)
        bind(encodingProducer, codecForAny)
    }

    fun <T> bind(codec: T) where T : EncodingProducer, T : Decoding {
        bind(codec, codec)
    }

    fun bind(encodingProducer: EncodingProducer, decoding: Decoding) {
        val tag = bindings.size
        require(tag < Byte.MAX_VALUE)
        bindings.add(
            Binding(
                tag = tag.toByte(),
                encodingForType = encodingProducer,
                decoding = decoding
            )
        )
    }

    private
    fun encodingForSubTypesOf(type: Class<*>, codec: Codec<Any>): EncodingProducer =
        { candidate -> codec.takeIf { type.isAssignableFrom(candidate) } }

    inline fun <reified T> bind(codec: Codec<T>) =
        bind(T::class.java, codec)

    inline fun <reified T> bind(serializer: Serializer<T>) =
        bind(T::class.java, serializer)

    fun bind(type: KClass<*>, codec: Codec<*>) =
        bind(type.java, codec)

    fun bind(type: KClass<*>, serializer: Serializer<*>) =
        bind(type.java, serializer)

    fun bind(type: Class<*>, serializer: Serializer<*>) =
        bind(type, SerializerCodec(serializer))
}

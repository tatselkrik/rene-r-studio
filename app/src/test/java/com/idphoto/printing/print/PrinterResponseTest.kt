package com.idphoto.printing.print

import com.hp.jipp.encoding.IppOutputStream
import com.hp.jipp.encoding.IppPacket
import com.hp.jipp.encoding.Tag
import com.hp.jipp.model.Types
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import org.junit.Assert.*
import org.junit.Test

class PrinterResponseTest {
    private fun packet(): ByteArray = ByteArrayOutputStream().also { bytes ->
        IppOutputStream(bytes).write(IppPacket.getPrinterAttributes(URI("ipps://printer.test/ipp/print"), Types.printerName).build())
    }.toByteArray()

    private fun framed(body: DataOutputStream.() -> Unit): ByteArray = ByteArrayOutputStream().also { bytes ->
        DataOutputStream(bytes).apply { write(byteArrayOf(1, 1, 0, 0, 0, 0, 0, 1)); body() }
    }.toByteArray()

    private fun DataOutputStream.value(tag: Int, length: Int = 0, name: String = "") {
        writeByte(tag); writeShort(name.length); writeBytes(name); writeShort(length)
        repeat(length) { writeByte(0) }
    }

    private fun rejects(bytes: ByteArray, message: String) {
        try { readPrinterResponse(bytes.inputStream(), timeoutMs = 1000); fail("Unsafe response accepted") }
        catch (error: IOException) { assertTrue(error.message.orEmpty(), error.message.orEmpty().contains(message)) }
    }

    @Test fun `normal encoded packet still reaches JIPP`() {
        val bytes = packet()
        val parsed = readPrinterResponse(bytes.inputStream(), bytes.size.toLong(), 1000)
        assertEquals(URI("ipps://printer.test/ipp/print"), parsed.getValue(Tag.operationAttributes, Types.printerUri))
    }

    @Test fun `normal media collection still decodes`() {
        val request = buildDirectPrintRequest(DirectPrinterProfile("printer.test"), "ID", PaperType.MATTE)
        val bytes = ByteArrayOutputStream().also { IppOutputStream(it).write(request) }.toByteArray()
        val parsed = readPrinterResponse(bytes.inputStream(), timeoutMs = 1000)
        assertEquals("photographic-matte", parsed.getValue(Tag.jobAttributes, Types.mediaCol)?.mediaType.toString())
    }

    @Test fun `oversized declared body is rejected before reading`() {
        val input = object : InputStream() { override fun read(): Int = error("Must not read oversized body") }
        try { readPrinterResponse(input, MAX_PRINTER_RESPONSE_BYTES + 1L, 1000); fail("Unsafe length accepted") }
        catch (error: IOException) { assertTrue(error.message.orEmpty().contains("too large")) }
    }

    @Test fun `chunked body cannot bypass the byte budget`() {
        rejects(ByteArray(MAX_PRINTER_RESPONSE_BYTES + 1), "too large")
    }

    @Test fun `deep collections are rejected before recursive parsing`() {
        rejects(framed {
            writeByte(1); value(0x34, name = "media-col")
            repeat(4095) {
                writeByte(0x4a); writeShort(0); writeShort(1); writeByte('a'.code)
                value(0x34)
            }
            repeat(4096) { value(0x37) }; writeByte(3)
        }, "nested")
    }

    @Test fun `delimiter floods cannot allocate unbounded attribute groups`() {
        rejects(framed { repeat(10_001) { writeByte(1) }; writeByte(3) }, "too many")
    }

    @Test fun `truncated values and unclosed collections are rejected`() {
        rejects(framed { writeByte(1); writeByte(0x44); writeShort(500) }, "invalid")
        rejects(framed { writeByte(1); value(0x34); writeByte(3) }, "invalid")
        rejects(packet().dropLast(1).toByteArray(), "invalid")
    }

    @Test fun `collection payload and extended tags cannot hide recursion`() {
        rejects(framed { writeByte(1); value(0x34, 10); writeByte(3) }, "invalid")
        rejects(framed { writeByte(1); writeByte(0x7f); writeInt(0x34); value(0x34); writeByte(3) }, "invalid")
    }

    @Test fun `slow and zero byte reads observe the response deadline`() {
        var time = 0L
        val input = object : InputStream() {
            override fun read(): Int = 0
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int { time += 1_000_000; return 0 }
        }
        try { readPrinterResponse(input, timeoutMs = 2, nanoTime = { time }); fail("Deadline not enforced") }
        catch (error: IOException) { assertTrue(error.message.orEmpty().contains("timed out")) }
    }
}

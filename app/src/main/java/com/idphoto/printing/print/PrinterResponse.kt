package com.idphoto.printing.print

import com.hp.jipp.encoding.IppInputStream
import com.hp.jipp.encoding.IppPacket
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

internal const val MAX_PRINTER_RESPONSE_BYTES = 1024 * 1024

/** Bound untrusted printer metadata before JIPP allocates or recursively decodes it. */
internal fun readPrinterResponse(
    input: InputStream,
    contentLength: Long = -1,
    timeoutMs: Int,
    nanoTime: () -> Long = System::nanoTime,
): IppPacket {
    if (contentLength > MAX_PRINTER_RESPONSE_BYTES) throw IOException("The printer response is too large.")
    require(timeoutMs > 0)
    val started = nanoTime()
    val limitNanos = timeoutMs.toLong() * 1_000_000
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        if (nanoTime() - started >= limitNanos) throw IOException("The printer response timed out.")
        val count = input.read(buffer)
        if (count < 0) break
        if (count > MAX_PRINTER_RESPONSE_BYTES - output.size()) throw IOException("The printer response is too large.")
        output.write(buffer, 0, count)
    }
    val bytes = output.toByteArray()
    validatePrinterAttributes(bytes)
    return IppInputStream(bytes.inputStream()).use { it.readPacket() }
}

/** Iterative wire framing check; never recurse into attacker-controlled collections. */
private fun validatePrinterAttributes(bytes: ByteArray) {
    fun invalid(): Nothing = throw IOException("The printer returned an invalid IPP response.")
    if (bytes.size < 9) invalid()
    var offset = 8 // IPP version, status and request identifier.
    var depth = 0
    var tokens = 0
    fun unsignedShort(): Int {
        if (bytes.size - offset < 2) invalid()
        return ((bytes[offset++].toInt() and 255) shl 8) or (bytes[offset++].toInt() and 255)
    }
    fun skip(count: Int) {
        if (count > bytes.size - offset) invalid()
        offset += count
    }
    while (offset < bytes.size) {
        if (++tokens > 10_000) throw IOException("The printer returned too many IPP attributes.")
        val tag = bytes[offset++].toInt() and 255
        if (tag <= 0x0f) {
            if (depth != 0) invalid()
            if (tag == 0x03) return // End-of-attributes; the HTTP body is already size-bounded.
            continue
        }
        // Extended tags are not decoded by the pinned JIPP reader. Do not let its
        // interpretation diverge from this framing/depth check.
        if (tag >= 0x7f) invalid()
        skip(unsignedShort()) // Attribute name.
        val valueLength = unsignedShort()
        if (tag == 0x34 || tag == 0x37) {
            if (valueLength != 0) invalid()
            if (tag == 0x34) {
                if (++depth > 32) throw IOException("The printer response contains too many nested collections.")
            } else if (--depth < 0) invalid()
        }
        skip(valueLength)
    }
    invalid()
}

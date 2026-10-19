package com.idphoto.printing.print

import java.io.IOException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.*
import org.junit.Test

class EpsonPrinterDiscoveryTest {
    @Test fun `discovery deadline reports failure without cancelling its caller`() = runBlocking {
        try {
            withDiscoveryTimeout(10) { awaitCancellation() }
            fail("Discovery must time out")
        } catch (error: IOException) {
            assertTrue(error.message.orEmpty().contains("timed out"))
            assertTrue(currentCoroutineContext().isActive)
        }
    }

    @Test fun `caller deadline is not converted into a printer failure`() = runBlocking {
        val result = withTimeoutOrNull(10) {
            withDiscoveryTimeout(1_000) { awaitCancellation() }
        }
        assertNull(result)
    }

    @Test fun `connection observer can process another event after discovery times out`() = runBlocking {
        val profile = DirectPrinterProfile("printer.test", certificateSha256 = "pin")
        var attempts = 0
        var stored: DirectPrinterProfile? = null
        val connection = PrinterConnection(load = { stored }, save = { stored = it }, discover = {
            if (attempts++ == 0) withDiscoveryTimeout(10) { awaitCancellation() }
            "printer.test"
        }, probe = { _, _ -> profile })

        val states = mutableListOf<PrinterConnectionState>()
        flowOf(Unit, Unit).collect {
            connection.refresh()
            states += connection.state.value
        }
        assertTrue(states.first().message.contains("timed out"))
        assertFalse(states.first().checking)
        assertEquals(profile, states.last().profile)
        assertEquals(profile, stored)
    }
}

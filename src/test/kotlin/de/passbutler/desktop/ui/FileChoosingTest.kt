package de.passbutler.desktop.ui

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class FileChoosingTest {
    @Test
    fun `Ensure already existing file extension results in same file instance`() {
        val exampleExtension = "foo"
        val exampleFile = File("./example.foo")

        val ensuredFile = runBlocking { exampleFile.ensureFileExtension(exampleExtension) }

        Assertions.assertSame(exampleFile, ensuredFile)
    }

    @Test
    fun `Ensure absent extension of file results in new file instance with extension`() {
        val exampleExtension = "foo"
        val exampleFile = File("./example.bar")

        val ensuredFile = runBlocking { exampleFile.ensureFileExtension(exampleExtension) }

        val expectedFile = File("./example.bar.foo")
        Assertions.assertEquals(expectedFile, ensuredFile)
    }

    @Test
    fun `Ensure absent extension of file with path results in new file instance with extension`() {
        val exampleExtension = "foo"
        val exampleFile = File("./this/is/an/example.bar")

        val ensuredFile = runBlocking { exampleFile.ensureFileExtension(exampleExtension) }

        val expectedFile = File("./this/is/an/example.bar.foo")
        Assertions.assertEquals(expectedFile, ensuredFile)
    }

    @Test
    fun `Ensure absent extension of file with absolute path results in new file instance with extension`() {
        val exampleExtension = "foo"
        val exampleFile = File("/this/is/an/example.bar")

        val ensuredFile = runBlocking { exampleFile.ensureFileExtension(exampleExtension) }

        val expectedFile = File("/this/is/an/example.bar.foo")
        Assertions.assertEquals(expectedFile, ensuredFile)
    }
}

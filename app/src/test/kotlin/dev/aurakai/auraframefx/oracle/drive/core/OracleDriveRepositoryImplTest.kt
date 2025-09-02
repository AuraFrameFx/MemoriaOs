package dev.aurakai.auraframefx.oracle.drive.core

/*
Testing stack note:
- This test is written for Kotlin coroutines using kotlinx-coroutines-test runTest.
- It uses MockK-style semantics; if the project uses Mockito, replace mockk/every/coEvery with Mockito equivalents.
- JUnit5 imports (org.junit.jupiter.*). If your project uses JUnit4, swap to @RunWith and org.junit.* accordingly.
*/

import android.content.Context
import dev.aurakai.auraframefx.oracle.drive.api.OracleCloudApi
import dev.aurakai.auraframefx.oracle.drive.model.OracleDriveFile
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Minimal DTOs to satisfy mapping in tests when real ones aren't visible from this scope.
// If your project provides these, remove these local test-only stubs and import the real types instead.
private data class ApiObject(val name: String, val size: Long, val timeCreated: String)
private data class ListFilesBody(val objects: List<ApiObject>)

@OptIn(ExperimentalCoroutinesApi::class)
class OracleDriveRepositoryImplTest {

    @MockK(relaxed = true) lateinit var oracleCloudApi: OracleCloudApi
    private lateinit var context: Context
    private lateinit var repo: OracleDriveRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        context = mockk(relaxed = true)
        // Construct repository under test
        repo = OracleDriveRepositoryImpl(oracleCloudApi, context)
    }

    @AfterEach
    fun tearDown() {
        // No global cleanup needed
    }

    @Nested
    @DisplayName("listFiles")
    inner class ListFilesTests {

        @Test
        fun `returns mapped files on successful response`() = runTest {
            val body = ListFilesBody(
                listOf(
                    ApiObject("a.txt", 10L, "2024-01-01T00:00:00Z"),
                    ApiObject("b.jpg", 20L, "2024-01-02T00:00:00Z")
                )
            )
            @Suppress("UNCHECKED_CAST")
            coEvery { oracleCloudApi.listFiles(bucketName = "bkt", prefix = "pre") } returns Response.success(body as Any as dev.aurakai.auraframefx.oracle.drive.model.ListFilesBody?)

            val result = repo.listFiles("bkt", "pre")
            assertEquals(2, result.size)
            assertEquals(OracleDriveFile("a.txt", 10L, "2024-01-01T00:00:00Z"), result[0])
            assertEquals(OracleDriveFile("b.jpg", 20L, "2024-01-02T00:00:00Z"), result[1])
        }

        @Test
        fun `returns emptyList when response unsuccessful`() = runTest {
            val errBody = "bad".toResponseBody("text/plain".toMediaTypeOrNull())
            @Suppress("UNCHECKED_CAST")
            coEvery { oracleCloudApi.listFiles(bucketName = any(), prefix = any()) } returns Response.error(500, errBody)

            val result = repo.listFiles("b", null)
            assertTrue(result.isEmpty())
        }

        @Test
        fun `returns emptyList when exception thrown`() = runTest {
            coEvery { oracleCloudApi.listFiles(any(), any()) } throws RuntimeException("boom")
            val result = repo.listFiles("b", "p")
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("uploadFile")
    inner class UploadFileTests {

        @Test
        fun `returns false when file does not exist`() = runTest {
            val nonExistent = File("build/tmp/nonexistent_${System.nanoTime()}.bin")
            assertFalse(nonExistent.exists())
            val ok = repo.uploadFile("bucket", "obj", nonExistent.absolutePath)
            assertFalse(ok)
        }

        @Test
        fun `returns true when api returns success`() = runTest {
            val tmp = File.createTempFile("upload_ok_", ".bin").apply { writeBytes(byteArrayOf(1,2,3)) }
            try {
                @Suppress("UNCHECKED_CAST")
                coEvery { oracleCloudApi.uploadFile(bucketName = any(), objectName = any(), body = any()) } returns Response.success<Unit>(null)

                val ok = repo.uploadFile("bucket", "obj", tmp.absolutePath)
                assertTrue(ok)
            } finally {
                tmp.delete()
            }
        }

        @Test
        fun `returns false when api returns error`() = runTest {
            val tmp = File.createTempFile("upload_err_", ".bin").apply { writeText("data") }
            try {
                val err = "err".toResponseBody("text/plain".toMediaTypeOrNull())
                @Suppress("UNCHECKED_CAST")
                coEvery { oracleCloudApi.uploadFile(any(), any(), any()) } returns Response.error(400, err)

                val ok = repo.uploadFile("b", "o", tmp.absolutePath)
                assertFalse(ok)
            } finally {
                tmp.delete()
            }
        }

        @Test
        fun `returns false when exception thrown`() = runTest {
            val tmp = File.createTempFile("upload_exc_", ".bin").apply { writeText("x") }
            try {
                coEvery { oracleCloudApi.uploadFile(any(), any(), any()) } throws IllegalStateException("fail")
                val ok = repo.uploadFile("b", "o", tmp.absolutePath)
                assertFalse(ok)
            } finally {
                tmp.delete()
            }
        }
    }

    @Nested
    @DisplayName("downloadFile")
    inner class DownloadFileTests {

        @Test
        fun `writes file to destination and returns file on success`() = runTest {
            val bytes = "hello world".toByteArray(StandardCharsets.UTF_8)
            val responseBody = object : ResponseBody() {
                override fun contentType() = "application/octet-stream".toMediaTypeOrNull()
                override fun contentLength() = bytes.size.toLong()
                override fun source() = okio.source(ByteArrayInputStream(bytes))
            }

            @Suppress("UNCHECKED_CAST")
            coEvery { oracleCloudApi.downloadFile(bucketName = "b", objectName = "obj.txt") } returns Response.success(responseBody)

            val destDir = createTempDir(prefix = "dl_ok_")
            try {
                val file = repo.downloadFile("b", "obj.txt", destDir.absolutePath)
                assertNotNull(file)
                assertTrue(file!!.exists())
                assertEquals("obj.txt", file.name)
                assertEquals("hello world", file.readText())
            } finally {
                destDir.deleteRecursively()
            }
        }

        @Test
        fun `returns null when response body is null`() = runTest {
            @Suppress("UNCHECKED_CAST")
            coEvery { oracleCloudApi.downloadFile(any(), any()) } returns Response.success<ResponseBody?>(null)

            val dest = createTempDir(prefix = "dl_null_")
            try {
                val file = repo.downloadFile("b", "x.bin", dest.absolutePath)
                assertNull(file)
            } finally {
                dest.deleteRecursively()
            }
        }

        @Test
        fun `returns null when response unsuccessful`() = runTest {
            val err = "nope".toResponseBody("text/plain".toMediaTypeOrNull())
            @Suppress("UNCHECKED_CAST")
            coEvery { oracleCloudApi.downloadFile(any(), any()) } returns Response.error(404, err)

            val dest = createTempDir(prefix = "dl_err_")
            try {
                val file = repo.downloadFile("b", "missing", dest.absolutePath)
                assertNull(file)
            } finally {
                dest.deleteRecursively()
            }
        }

        @Test
        fun `returns null when exception thrown`() = runTest {
            coEvery { oracleCloudApi.downloadFile(any(), any()) } throws RuntimeException("io fail")

            val dest = createTempDir(prefix = "dl_exc_")
            try {
                val file = repo.downloadFile("b", "x", dest.absolutePath)
                assertNull(file)
            } finally {
                dest.deleteRecursively()
            }
        }
    }

    @Nested
    @DisplayName("deleteFile")
    inner class DeleteFileTests {

        @Test
        fun `returns true on successful delete`() = runTest {
            @Suppress("UNCHECKED_CAST")
            coEvery { oracleCloudApi.deleteFile(bucketName = "b", objectName = "o") } returns Response.success<Unit>(null)

            val ok = repo.deleteFile("b", "o")
            assertTrue(ok)
        }

        @Test
        fun `returns false on unsuccessful delete`() = runTest {
            val err = "bad".toResponseBody("text/plain".toMediaTypeOrNull())
            @Suppress("UNCHECKED_CAST")
            coEvery { oracleCloudApi.deleteFile(any(), any()) } returns Response.error(500, err)

            val ok = repo.deleteFile("b", "o")
            assertFalse(ok)
        }

        @Test
        fun `returns false when exception thrown`() = runTest {
            coEvery { oracleCloudApi.deleteFile(any(), any()) } throws RuntimeException("boom")

            val ok = repo.deleteFile("b", "o")
            assertFalse(ok)
        }
    }
}
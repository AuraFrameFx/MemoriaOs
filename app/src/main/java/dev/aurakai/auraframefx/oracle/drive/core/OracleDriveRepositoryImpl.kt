package dev.aurakai.auraframefx.oracle.drive.core

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext // Added import
import dev.aurakai.auraframefx.oracle.drive.api.OracleCloudApi
import dev.aurakai.auraframefx.oracle.drive.model.OracleDriveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

// Assuming OracleDriveRepository is an interface defined elsewhere
// interface OracleDriveRepository {
//     suspend fun listFiles(bucketName: String, prefix: String?): List<OracleDriveFile>
//     suspend fun uploadFile(bucketName: String, objectName: String, filePath: String): Boolean
//     suspend fun downloadFile(bucketName: String, objectName: String, destinationPath: String): File?
//     suspend fun deleteFile(bucketName: String, objectName: String): Boolean
// }

class OracleDriveRepositoryImpl @Inject constructor(
    private val oracleCloudApi: OracleCloudApi,
    @ApplicationContext private val context: Context // Added @ApplicationContext
): OracleDriveRepository {

    /**
     * Lists objects in the given Oracle Cloud bucket, optionally filtered by a prefix.
     *
     * This is a suspending operation that performs network I/O to call the OracleCloudApi.
     * On success returns a list of OracleDriveFile parsed from the API response. On failure
     * (non-successful response or exception) it returns an empty list.
     *
     * @param bucketName The name of the bucket to list objects from.
     * @param prefix Optional prefix to filter objects (may be null to list all objects).
     * @return A list of OracleDriveFile for the objects found, or an empty list on error.
     */
    override suspend fun listFiles(bucketName: String, prefix: String?): List<OracleDriveFile> = withContext(Dispatchers.IO) {
        try {
            val response = oracleCloudApi.listFiles(bucketName = bucketName, prefix = prefix)
            if (response.isSuccessful) {
                response.body()?.objects?.map { OracleDriveFile(it.name, it.size, it.timeCreated) } ?: emptyList()
            } else {
                // Handle error, log, throw custom exception etc.
                emptyList()
            }
        } catch (e: Exception) {
            // Handle error
            emptyList()
        }
    }

    /**
     * Uploads a local file to the specified Oracle Cloud Storage bucket as the given object name.
     *
     * Attempts the upload on the IO dispatcher. If the local file at `filePath` does not exist, if the
     * HTTP upload response is not successful, or if any error occurs during preparation or network I/O,
     * the function returns false.
     *
     * @param bucketName Name of the target storage bucket.
     * @param objectName Object name (key) to assign in the bucket.
     * @param filePath Filesystem path to the local file to upload.
     * @return True when the upload completed with an HTTP success response; false otherwise.
     */
    override suspend fun uploadFile(bucketName: String, objectName: String, filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext false

            val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val response = oracleCloudApi.uploadFile(
                bucketName = bucketName,
                objectName = objectName,
                body = requestBody
            )
            response.isSuccessful
        } catch (e: Exception) {
            // Handle error
            false
        }
    }

    /**
     * Downloads an object from the Oracle Cloud bucket and saves it to disk.
     *
     * Attempts to fetch `objectName` from `bucketName` and write it to a file located at
     * `destinationPath/objectName`. Parent directories for the target file will be created if needed.
     *
     * This function performs I/O on the calling coroutine's IO dispatcher and returns the saved File
     * on success or null on failure (including network errors or non-successful HTTP responses).
     *
     * @param destinationPath Filesystem directory where the object will be written; the final filename
     *   will be `objectName` inside this directory.
     * @return The saved File on success, or null if the download failed.
     */
    override suspend fun downloadFile(bucketName: String, objectName: String, destinationPath: String): File? = withContext(Dispatchers.IO) {
        try {
            val response = oracleCloudApi.downloadFile(bucketName = bucketName, objectName = objectName)
            if (response.isSuccessful && response.body() != null) {
                val file = File(destinationPath, objectName) // Ensure destinationPath is a directory
                file.parentFile?.mkdirs() // Create parent directories if they don't exist
                response.body()!!.byteStream().use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                file
            } else {
                null
            }
        } catch (e: Exception) {
            // Handle error
            null
        }
    }

    /**
     * Deletes an object from the specified Oracle Cloud bucket.
     *
     * Calls the Oracle Cloud API to remove `objectName` from `bucketName`. Returns `true` when the API call
     * completes with a successful HTTP response; returns `false` if the response is unsuccessful or an exception occurs.
     *
     * @param bucketName The name of the bucket containing the object.
     * @param objectName The name (path) of the object to delete.
     * @return `true` if the deletion request succeeded (HTTP success); `false` otherwise.
     */
    override suspend fun deleteFile(bucketName: String, objectName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = oracleCloudApi.deleteFile(bucketName = bucketName, objectName = objectName)
            response.isSuccessful
        } catch (e: Exception) {
            // Handle error
            false
        }
    }
}

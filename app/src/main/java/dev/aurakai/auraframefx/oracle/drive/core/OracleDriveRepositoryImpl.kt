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
     * Retrieves objects from the specified bucket and returns them as a list of OracleDriveFile.
     *
     * If the optional `prefix` is provided, only objects whose names start with that prefix are returned.
     * On API failure or any exception, an empty list is returned.
     *
     * @param prefix Optional object name prefix used to filter results; `null` or omitted fetches all objects.
     * @return A list of matching OracleDriveFile entries, or an empty list if the request fails or an error occurs.
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
     * Uploads a local file to the specified bucket as the given object name.
     *
     * Reads the file at [filePath] and streams it to the remote store; returns true when the HTTP upload
     * request completes successfully. Returns false if the local file does not exist, the upload fails,
     * or an exception occurs.
     *
     * @param bucketName Destination bucket in the remote storage.
     * @param objectName Desired object name/key for the uploaded file.
     * @param filePath Absolute or relative path to the local file to be uploaded.
     * @return true if the upload completed successfully; false otherwise.
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
     * Downloads an object from the Oracle Cloud bucket and writes it to disk.
     *
     * The function requests `objectName` from `bucketName` and, on a successful response with a body,
     * saves the bytes to a file named `objectName` under `destinationPath`. `destinationPath` is treated
     * as a directory; parent directories will be created if missing. Returns the saved File on success
     * or null if the download fails or an error occurs.
     *
     * @param bucketName The Oracle Cloud bucket containing the object.
     * @param objectName The object key to download; also used as the filename when saving.
     * @param destinationPath Directory path where the file will be written.
     * @return The written File on success, or null on failure.
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
     * Deletes an object from the specified Oracle Cloud Storage bucket.
     *
     * Performs the operation asynchronously and returns true when the API reports success.
     *
     * @param bucketName The name of the bucket containing the object.
     * @param objectName The name (key) of the object to delete.
     * @return True if the delete request succeeded; false if the request failed or an error occurred.
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

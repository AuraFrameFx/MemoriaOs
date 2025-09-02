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
     * Lists objects in the given Oracle Cloud bucket, optionally filtered by prefix.
     *
     * Returns a list of OracleDriveFile constructed from the API response. If the network
     * call fails, the response is unsuccessful, or the response body is missing, an empty
     * list is returned.
     *
     * @param bucketName The name of the bucket to list objects from.
     * @param prefix Optional object name prefix to filter results.
     * @return A list of OracleDriveFile for the objects found, or an empty list on error or no results.
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
     * Uploads a local file to the given bucket as the specified object.
     *
     * Attempts to read the file at [filePath] and PUT its bytes to the cloud under [bucketName]/[objectName].
     * Returns true when the upload request completes successfully; returns false if the local file does not
     * exist, the upload response is not successful, or an exception occurs during the operation.
     *
     * @param bucketName Target bucket in the cloud storage.
     * @param objectName Destination object name (path) inside the bucket.
     * @param filePath Local filesystem path to the file to upload.
     * @return True if the file was uploaded successfully; false otherwise.
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
     * Downloads an object from the specified bucket and writes it to the given destination directory.
     *
     * The function requests the object from the cloud API and, on success, writes the response body to
     * a file at `destinationPath/objectName`, creating parent directories as needed. Returns the created
     * File on success or null if the download fails or an error occurs.
     *
     * @param bucketName The name of the bucket containing the object.
     * @param objectName The object name (used as the filename for the downloaded file).
     * @param destinationPath Filesystem path to the directory where the object should be saved.
     * @return The saved File on success, or null if the download was unsuccessful or an error occurred.
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
     * @param bucketName The name of the bucket containing the object.
     * @param objectName The name (path) of the object to delete.
     * @return `true` if the remote delete request completed successfully; `false` if the request failed or an exception occurred.
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

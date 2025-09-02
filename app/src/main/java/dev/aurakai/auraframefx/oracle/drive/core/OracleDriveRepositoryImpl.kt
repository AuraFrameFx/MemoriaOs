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
     * Lists objects in the specified cloud bucket, optionally filtered by a prefix.
     *
     * Performs a network request to retrieve object metadata and maps results to a list of [OracleDriveFile].
     *
     * @param bucketName The name of the bucket to list objects from.
     * @param prefix Optional prefix to filter objects by (treats the bucket like a directory).
     * @return A list of [OracleDriveFile] for the objects found. Returns an empty list if no objects are found or on failure.
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
     * Uploads a local file to the specified bucket and object path in Oracle Cloud.
     *
     * @param bucketName The target bucket in Oracle Cloud where the object will be stored.
     * @param objectName The name (key) to assign to the uploaded object inside the bucket.
     * @param filePath The local filesystem path to the file to upload. If the file does not exist, the function returns false.
     * @return True if the upload request completed successfully (HTTP success), false otherwise.
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
     * Downloads an object from the specified bucket and saves it to disk.
     *
     * The response body is written to a file named `objectName` inside `destinationPath`.
     * Parent directories are created if missing. On failure (network error, non-successful response,
     * missing body, or I/O error) the function returns null.
     *
     * @param bucketName The cloud bucket containing the object.
     * @param objectName The name of the object to download; also used as the filename for the saved file.
     * @param destinationPath Filesystem path to a directory where the downloaded file will be saved.
     * @return The saved File on success, or null on failure.
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
     * Deletes an object from the specified bucket in Oracle Cloud Storage.
     *
     * Calls the cloud API to remove `objectName` from `bucketName` and returns whether the operation succeeded.
     * If the network call fails or an exception occurs, the function returns false.
     *
     * @param bucketName The name of the bucket containing the object.
     * @param objectName The name (key) of the object to delete.
     * @return `true` if the delete request completed successfully (HTTP success); `false` on failure or exception.
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

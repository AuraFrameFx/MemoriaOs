package dev.aurakai.auraframefx.oracle.drive.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
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
) : OracleDriveRepository {

    /**
         * Lists files in the given Oracle Cloud bucket, optionally filtered by a prefix.
         *
         * Performs the network request on Dispatchers.IO and returns a list of mapped OracleDriveFile domain objects.
         * If the API response is unsuccessful or an exception occurs, an empty list is returned.
         *
         * @param bucketName The name of the bucket to list objects from.
         * @param prefix Optional object name prefix to filter results; pass null to list all objects.
         * @return A list of OracleDriveFile instances representing the objects found (may be empty).
         */
        override suspend fun listFiles(bucketName: String, prefix: String?): List<OracleDriveFile> =
        withContext(Dispatchers.IO) {
            try {
                val response = oracleCloudApi.listFiles(bucketName = bucketName, prefix = prefix)
                if (response.isSuccessful) {
                    response.body()?.objects?.map {
                        OracleDriveFile(
                            it.name,
                            it.size,
                            it.timeCreated
                        )
                    } ?: emptyList()
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
     * Uploads a local file to the specified Oracle Cloud bucket as the given object.
     *
     * @param bucketName Destination bucket name.
     * @param objectName Target object name (path) inside the bucket.
     * @param filePath Local filesystem path to the file to upload; returns false if the file does not exist.
     * @return True when the upload request returns a successful HTTP response; false on failure or exception.
     */
    override suspend fun uploadFile(
        bucketName: String,
        objectName: String,
        filePath: String
    ): Boolean = withContext(Dispatchers.IO) {
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
     * Performs the network request on Dispatchers.IO, writes the response byte stream to a file,
     * and returns the resulting File on success or null on failure. To prevent path traversal,
     * only the basename of `objectName` is used as the destination filename; parent directories
     * under `destinationPath` are created if missing.
     *
     * @param bucketName The name of the bucket containing the object.
     * @param objectName The object key in the bucket; only its basename is used for the saved file.
     * @param destinationPath Path to the directory where the downloaded file will be written.
     * @return The created File on success, or null if the download or write failed.
     */
    override suspend fun downloadFile(
        bucketName: String,
        objectName: String,
        destinationPath: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val response =
                oracleCloudApi.downloadFile(bucketName = bucketName, objectName = objectName)
            if (response.isSuccessful && response.body() != null) {
                // Normalize objectName to its basename to prevent path traversal
                val safeName = File(objectName).name // strips any path components
                val file = File(destinationPath, safeName) // Ensure destinationPath is a directory
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
         * Delete an object from the specified Oracle Cloud bucket.
         *
         * Executes the deletion on the IO dispatcher and returns whether the remote API reported success.
         * Any exceptions or non-successful responses result in `false`.
         *
         * @param bucketName Name of the target bucket.
         * @param objectName Name (path) of the object to delete.
         * @return `true` if the API response indicates the object was deleted; `false` on failure or error.
         */
        override suspend fun deleteFile(bucketName: String, objectName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val response =
                    oracleCloudApi.deleteFile(bucketName = bucketName, objectName = objectName)
                response.isSuccessful
            } catch (e: Exception) {
                // Handle error
                false
            }
        }
}

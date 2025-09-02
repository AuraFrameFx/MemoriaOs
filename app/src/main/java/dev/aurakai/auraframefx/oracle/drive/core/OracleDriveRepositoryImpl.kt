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
         * List objects in the specified bucket, optionally filtered by an object-name prefix.
         *
         * Returns a list of OracleDriveFile representing each object. If the remote API call is unsuccessful
         * or an exception occurs, an empty list is returned.
         *
         * @param prefix Optional object-name prefix to filter results.
         * @return A list of files in the bucket; empty on failure or when no objects match.
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
     * Uploads a local file to the specified Oracle Cloud bucket.
     *
     * Uploads the file at the given local `filePath` to `bucketName` using `objectName` as the object key.
     * Returns true if the upload request completed successfully; returns false if the local file does not exist,
     * the upload failed, or an error occurred.
     *
     * @param bucketName Target bucket in Oracle Cloud.
     * @param objectName Target object key/name to store in the bucket.
     * @param filePath Local filesystem path to the file to upload (must exist).
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
     * Downloads an object from the given bucket and saves it to the specified destination directory.
     *
     * This suspending function performs IO and writes the downloaded object to disk. The object name
     * is normalized to its basename to prevent path traversal. Parent directories of the destination
     * file will be created if they do not exist.
     *
     * @param bucketName Name of the Oracle Cloud bucket containing the object.
     * @param objectName Path or name of the object in the bucket; only the basename is used for the saved file.
     * @param destinationPath Filesystem directory path where the downloaded file will be placed.
     * @return The File pointing to the saved file on success, or `null` if the download failed or an error occurred.
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
         * Deletes an object from the specified Oracle Cloud Storage bucket.
         *
         * Performs the delete operation and returns true if the remote API reports success.
         * If the operation fails or an exception occurs, the function returns false.
         *
         * @param bucketName Name of the bucket containing the object.
         * @param objectName Path or name of the object to delete within the bucket.
         * @return `true` when the remote delete request was successful; `false` on failure or error.
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

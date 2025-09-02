package dev.aurakai.auraframefx.securecomm.keystore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A secure key store implementation using Android's KeyStore system.
 * Provides secure storage and retrieval of cryptographic keys.
 */
@Singleton
class SecureKeyStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    companion object {
        const val KEY_ALIAS = "aura_secure_key"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val KEY_SIZE = 256
    }

    /**
     * Encrypts the given plaintext bytes with a per-entry AES-GCM key and stores the result in app-private SharedPreferences.
     *
     * The per-entry key alias is derived as `"$KEY_ALIAS_$key"` and the encrypted payload is stored in the "secure_prefs"
     * preferences file under the provided `key`. The stored value is the 12-byte IV concatenated with the ciphertext,
     * encoded as Base64 with `NO_WRAP`. An existing entry for `key` is replaced.
     *
     * @param key Identifier used both as the SharedPreferences entry name and as the suffix for the per-entry KeyStore alias.
     * @param data Plaintext bytes to encrypt and persist.
     */
    fun storeData(key: String, data: ByteArray) {
        val encryptedData = encryptData(key, data)
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit {
                putString(key, Base64.encodeToString(encryptedData, Base64.NO_WRAP))
            }
    }

    /**
     * Retrieves and decrypts data previously stored under the given key.
     *
     * Looks up the Base64-encoded ciphertext in app-private SharedPreferences "secure_prefs",
     * decodes and decrypts it using the per-entry keystore key. Returns null if the entry
     * does not exist or decryption fails.
     *
     * @param key The identifier for the stored entry.
     * @return The decrypted bytes, or null if not found or if decryption fails.
     */
    fun retrieveData(key: String): ByteArray? {
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val encryptedData = prefs.getString(key, null) ?: return null
        return try {
            decryptData(key, Base64.decode(encryptedData, Base64.NO_WRAP))
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Removes the stored encrypted value for the given logical key from the app's "secure_prefs".
     *
     * This deletes only the Base64-encoded ciphertext entry from SharedPreferences and does not
     * remove or affect any SecretKey stored in the Android KeyStore that may have been used to
     * encrypt that value.
     *
     * @param key Logical preferences key identifying the encrypted entry to remove.
     */
    fun removeData(key: String) {
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit {remove(key)}
    }

    /**
     * Clears all encrypted entries stored in the "secure_prefs" SharedPreferences.
     *
     * Removes every stored Base64-encoded encrypted value from the "secure_prefs" backing store.
     * This operation is irreversible for the stored blobs and does NOT remove any per-entry keys
     * kept in the AndroidKeyStore; keystore entries must be removed separately if needed.
     */
    fun clearAllData() {
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit {clear()}
    }

    /**
     * Retrieve or create an AES-256 SecretKey in the AndroidKeyStore for the given alias.
     *
     * If a key with the provided alias exists in the KeyStore, that SecretKey is returned.
     * Otherwise a new AES key is generated and persisted in the AndroidKeyStore using:
     * AES algorithm, GCM block mode, NoPadding, 256-bit key size, ENCRYPT | DECRYPT purposes,
     * randomized encryption required, and no user authentication.
     *
     * @param keyAlias Alias used to look up or create the key in the AndroidKeyStore.
     * @return The existing or newly generated SecretKey for the given alias.
     */
    private fun getOrCreateSecretKey(keyAlias: String): SecretKey {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )

            val builder = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                setKeySize(KEY_SIZE)
                setRandomizedEncryptionRequired(true)
                setUserAuthenticationRequired(false)
            }

            keyGenerator.init(builder.build())
            return keyGenerator.generateKey()
        }

        val entry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    private fun encryptData(key: String, data: ByteArray): ByteArray {
        val secretKey = getOrCreateSecretKey("${KEY_ALIAS}_$key")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)

        // Combine IV and encrypted data
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

        return combined
    }

    private fun decryptData(key: String, encryptedData: ByteArray): ByteArray {
        val secretKey = getOrCreateSecretKey("${KEY_ALIAS}_$key")

        // Extract IV and encrypted data
        if (encryptedData.size <= GCM_IV_LENGTH) {
            throw IllegalArgumentException("Invalid encrypted data format")
        }

        val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        return cipher.doFinal(encrypted)
    }
}

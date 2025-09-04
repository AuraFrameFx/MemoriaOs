package dev.aurakai.auraframefx.securecomm.keystore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
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
        private const val KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    /**
     * Encrypts and stores a byte array under a named entry in private SharedPreferences.
     *
     * The data is encrypted with an AES-GCM SecretKey derived from a composite alias based on the provided
     * key; the stored value is the IV concatenated with the ciphertext and saved as a Base64 string in
     * the "secure_prefs" SharedPreferences (MODE_PRIVATE).
     *
     * @param key Identifier used both as the SharedPreferences entry name and to derive the per-entry key alias.
     * @param data Plaintext bytes to encrypt and persist.
     */
    fun storeData(key: String, data: ByteArray) {
        val encryptedData = encryptData(key, data)
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString(key, Base64.encodeToString(encryptedData, Base64.NO_WRAP))
            .apply()
    }

    /**
     * Retrieves and decrypts data previously stored under the given key.
     *
     * Returns the plaintext byte array for the stored entry, or null if the key is not present
     * or if decoding/decryption fails.
     *
     * @param key The identifier used to locate the stored encrypted value.
     * @return Decrypted data as a ByteArray, or null when not found or on failure.
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
     * Removes the encrypted entry associated with the given key from persistent secure storage.
     *
     * @param key Identifier of the stored value to remove (the entry in "secure_prefs").
     */
    fun removeData(key: String) {
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove(key).apply()
    }

    /**
     * Removes all entries from the secure preferences store ("secure_prefs").
     *
     * This deletes all stored encrypted values but does not remove any keys from the AndroidKeyStore.
     */
    fun clearAllData() {
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Retrieve a SecretKey for the given alias from the AndroidKeyStore, creating and storing one if missing.
     *
     * If the alias does not exist in the keystore this generates a new AES key (KEY_SIZE, AES/GCM/NoPadding)
     * with purposes ENCRYPT | DECRYPT, randomized encryption required, and no user authentication, then returns it.
     * If the alias exists, returns the existing SecretKeyEntry.secretKey.
     *
     * @param keyAlias Alias used to lookup or create the key inside the AndroidKeyStore.
     * @return The SecretKey associated with the provided alias.
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

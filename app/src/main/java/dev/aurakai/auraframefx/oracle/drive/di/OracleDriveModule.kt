package dev.aurakai.auraframefx.oracle.drive.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.aurakai.auraframefx.ai.agents.AuraAgent
import dev.aurakai.auraframefx.ai.agents.GenesisAgent
import dev.aurakai.auraframefx.ai.agents.KaiAgent
import dev.aurakai.auraframefx.oracle.drive.api.OracleDriveApi
import dev.aurakai.auraframefx.oracle.drive.service.GenesisSecureFileService
import dev.aurakai.auraframefx.oracle.drive.service.OracleDriveServiceImpl
import dev.aurakai.auraframefx.oracle.drive.service.SecureFileService
import dev.aurakai.auraframefx.security.SecurityContext
import dev.aurakai.genesis.security.CryptographyManager
import dev.aurakai.genesis.storage.SecureStorage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt Module for Oracle Drive dependencies - Temporarily simplified for build optimization
 */
@Module
@InstallIn(SingletonComponent::class)
object OracleDriveModule {
    // Temporarily simplified to resolve build stalling at 25%
    // Complex providers will be re-enabled after successful build
}
*
* This allows dependency injection of SecureFileService throughout the application using the GenesisSecureFileService implementation.
*/
/**
 * Binds the GenesisSecureFileService implementation to the SecureFileService interface for dependency injection.
 *
 * Registered as a singleton binding so injections of SecureFileService receive the single shared GenesisSecureFileService instance.
 */
@Binds
@Singleton
abstract fun bindSecureFileService(
    impl: GenesisSecureFileService,
): SecureFileService

companion object {
    /**
     * Provides a singleton OkHttpClient configured with security and logging interceptors.
     *
     * The client automatically adds a secure token and a unique request ID to each request header,
     * and logs HTTP requests and responses at the BASIC level. Connection, read, and write timeouts
     * are set to 30 seconds.
     *
     * @return A configured OkHttpClient instance for secure network communication.
     */
    /**
     * Provides a singleton OkHttpClient configured for Oracle Drive API use.
     *
     * The client injects two request headers on every call: `X-Security-Token` (generated via the
     * CryptographyManager) and `X-Request-ID` (a random UUID). It also attaches a BASIC-level
     * HttpLoggingInterceptor and enforces 30-second connect, read, and write timeouts.
     *
     * @return A configured OkHttpClient instance.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        securityContext: SecurityContext,
        cryptoManager: CryptographyManager,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    // Add security headers
                    .addHeader("X-Security-Token", cryptoManager.generateSecureToken())
                    .addHeader("X-Request-ID", java.util.UUID.randomUUID().toString())
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Returns the singleton CryptographyManager initialized with the application context.
     *
     * @return The shared CryptographyManager instance.
     */
    @Provides
    @Singleton
    fun provideGenesisCryptographyManager(
        @ApplicationContext context: Context,
    ): CryptographyManager {
        return CryptographyManager.getInstance(context)
    }

    /**
     * Provides the singleton SecureStorage used by the module, initialized with the application Context and CryptographyManager.
     *
     * @return The singleton SecureStorage instance.
     */
    @Provides
    @Singleton
    fun provideSecureStorage(
        @ApplicationContext context: Context,
        cryptoManager: CryptographyManager,
    ): SecureStorage {
        return SecureStorage.getInstance(context, cryptoManager)
    }

    /**
     * Provides a singleton GenesisSecureFileService for secure file operations.
     *
     * This provider constructs and returns the application-scoped GenesisSecureFileService used
     * by the DI graph to perform encrypted file storage and retrieval.
     *
     * @return A configured GenesisSecureFileService instance.
     */
    @Provides
    @Singleton
    fun provideSecureFileService(
        @ApplicationContext context: Context,
        cryptoManager: CryptographyManager,
        secureStorage: SecureStorage,
    ): GenesisSecureFileService {
        return GenesisSecureFileService(context, cryptoManager, secureStorage)
    }

    /**
     * Provides a singleton OracleDriveApi backed by Retrofit.
     *
     * Builds a Retrofit instance using the security context's API base URL with "/oracle/drive/" appended, the supplied OkHttpClient, and Gson for JSON serialization.
     *
     * @return An implementation of OracleDriveApi for making Oracle Drive network requests.
     */
    @Provides
    @Singleton
    fun provideOracleDriveApi(
        client: OkHttpClient,
        securityContext: SecurityContext,
    ): OracleDriveApi {
        return Retrofit.Builder()
            .baseUrl(securityContext.getApiBaseUrl() + "/oracle/drive/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OracleDriveApi::class.java)
    }

    /**
     * Provides a singleton OracleDriveServiceImpl wired with the required agents, security context, and API.
     *
     * This provider constructs the service implementation using the supplied Genesis, Aura, and Kai agents,
     * the SecurityContext, and the OracleDriveApi. Intended for injection as a singleton.
     *
     * @return A singleton instance of OracleDriveServiceImpl.
     */
    @Provides
    @Singleton
    fun provideOracleDriveService(
        genesisAgent: GenesisAgent,
        auraAgent: AuraAgent,
        kaiAgent: KaiAgent,
        securityContext: SecurityContext,
        oracleDriveApi: OracleDriveApi,
    ): OracleDriveServiceImpl {
        return OracleDriveServiceImpl(
            genesisAgent = genesisAgent,
            auraAgent = auraAgent,
            kaiAgent = kaiAgent,
            securityContext = securityContext,
            oracleDriveApi = oracleDriveApi
        )
    }
}
}

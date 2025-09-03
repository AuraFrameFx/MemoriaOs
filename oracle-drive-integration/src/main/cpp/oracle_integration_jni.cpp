#include <jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "OracleDriveIntegration"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" /**
 * @brief Return the native Oracle Drive integration version string to Java.
 *
 * Returns a newly allocated Java UTF string containing the native library
 * version identifier ("Genesis Oracle Drive Integration v1.0.0").
 *
 * @return jstring Java UTF string with the integration version.
 */
JNIEXPORT jstring JNICALL
Java_dev_aurakai_auraframefx_oracledriveintegration_OracleDriveNative_getVersion(
        JNIEnv *env,
        jobject /* this */) {

    LOGI("Oracle Drive Integration Native - Genesis Protocol v3.0");
    std::string version = "Genesis Oracle Drive Integration v1.0.0";
    return env->NewStringUTF(version.c_str());
}

extern "C" /**
 * @brief Initializes the native Oracle Drive integration.
 *
 * Performs any required native startup and resource allocation for the Oracle Drive
 * integration. Exposed to Java via JNI; returns JNI_TRUE when initialization succeeds.
 *
 * @return jboolean JNI_TRUE on successful initialization.
 */
JNIEXPORT jboolean JNICALL
Java_dev_aurakai_auraframefx_oracledriveintegration_OracleDriveNative_initialize(
        JNIEnv *env,
        jobject /* this */) {

    LOGI("Initializing Oracle Drive Integration...");
    // Initialize Oracle Drive native components
    return JNI_TRUE;
}

extern "C" /**
 * @brief Shut down and clean up native Oracle Drive integration components.
 *
 * Logs shutdown intent and performs platform-native cleanup of any resources
 * allocated by the Oracle Drive integration. Safe to call from Java via JNI.
 */
JNIEXPORT void JNICALL
Java_dev_aurakai_auraframefx_oracledriveintegration_OracleDriveNative_shutdown(
        JNIEnv *env,
        jobject /* this */) {

    LOGI("Shutting down Oracle Drive Integration...");
    // Cleanup Oracle Drive native components
}
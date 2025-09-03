// app/src/main/cpp/native-lib.cpp

#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "CascadeAIService-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// This is a sample JNI function.
// The name MUST follow the pattern: Java_your_package_name_YourActivityName_yourMethodName

// Global reference to the JavaVM
static JavaVM *g_vm = nullptr;

// Global reference to the context
static jobject g_context = nullptr;

extern "C" /**
 * @brief Initialize native JNI state for CascadeAIService.
 *
 * Stores the process-wide JavaVM and (if provided) creates a global reference to the supplied
 * Android Context so native code can safely access the JVM and Context after this call.
 *
 * On failure to obtain the JavaVM or to create the global Context reference, the function logs
 * an error and returns early without completing initialization. Successful completion sets the
 * globals `g_vm` and, when `context` is non-null, `g_context`.
 */
JNIEXPORT void JNICALL
Java_dev_aurakai_auraframefx_ai_services_CascadeAIService_nativeInitialize(
        JNIEnv *env,
        jclass clazz,
        jobject context) {
    // Store the JavaVM for later use
    if (env->GetJavaVM(&g_vm) != JNI_OK) {
        LOGE("Failed to get JavaVM");
        return;
    }

    // Create a global reference to the context
    if (context != nullptr) {
        g_context = env->NewGlobalRef(context);
        if (g_context == nullptr) {
            LOGE("Failed to create global reference to context");
            return;
        }
    }

    LOGI("Native initialization complete");
}

extern "C" /**
 * @brief Processes a request sent from Java and returns a JSON-like response string.
 *
 * Converts the provided Java UTF-8 string into a native C string, performs placeholder
 * processing, and returns the result as a new UTF-8 Java string.
 *
 * If `request` is null or cannot be converted to UTF-8, the function returns a JSON-like
 * error string. The native UTF-8 buffer obtained from the Java string is always released
 * before returning.
 *
 * @param request UTF-8 Java string containing the request payload; may be null.
 * @return jstring New Java UTF-8 string containing either a JSON-like success response
 *         (e.g. {"content":"Request processed by native code","confidence":0.9})
 *         or a JSON-like error object when input is invalid or conversion fails.
 */
JNIEXPORT jstring JNICALL
Java_dev_aurakai_auraframefx_ai_services_CascadeAIService_nativeProcessRequest(
        JNIEnv *env,
        jclass clazz,
        jstring request) {
    if (request == nullptr) {
        LOGE("Request string is null");
        return env->NewStringUTF("{'error':'Invalid request'}");
    }

    const char *requestStr = env->GetStringUTFChars(request, nullptr);
    if (requestStr == nullptr) {
        LOGE("Failed to get request string");
        return env->NewStringUTF("{'error':'Failed to process request'}");
    }

    // Process the request (this is where you'd add your actual processing logic)
    LOGI("Processing request: %s", requestStr);
    
    // Example response
    const char *response = "{'content':'Request processed by native code', 'confidence':0.9}";
    
    // Release the string
    env->ReleaseStringUTFChars(request, requestStr);
    
    return env->NewStringUTF(response);
}

extern "C" /**
 * @brief Release native resources and clear retained Java global references for the service.
 *
 * Deletes the process-wide global reference to the Android context (g_context) if present
 * and resets it to nullptr. Safe to call multiple times; subsequent calls after the first
 * will be no-ops for the context cleanup.
 */
JNIEXPORT void JNICALL
Java_dev_aurakai_auraframefx_ai_services_CascadeAIService_nativeShutdown(
        JNIEnv *env,
        jclass clazz) {
    LOGI("Shutting down native service");
    
    // Clean up global references
    if (g_context != nullptr) {
        env->DeleteGlobalRef(g_context);
        g_context = nullptr;
    }
}

/**
 * @brief JNI library load handler — validates the JNI version and caches the JavaVM.
 *
 * Checks that the JVM supports JNI_VERSION_1_6, stores the provided JavaVM pointer in the
 * process-global g_vm for later native-to-Java interactions, and returns the negotiated
 * JNI version on success.
 *
 * @param vm Pointer to the JavaVM provided by the runtime.
 * @param reserved Reserved for future use; ignored.
 * @return jint JNI_VERSION_1_6 on success, or JNI_ERR if the required JNI version is not available.
 */
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    
    // Store the JavaVM for later use
    g_vm = vm;
    
    return JNI_VERSION_1_6;
}

/**
 * @brief Called when the JNI library is unloaded; releases retained global JNI references.
 *
 * Obtains a JNIEnv for JNI_VERSION_1_6 and, if available, deletes the stored global
 * reference to the Android context (g_context) and clears the pointer. If the JNI
 * environment cannot be acquired, the function returns without making changes.
 */
JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    
    // Clean up global references
    if (g_context != nullptr) {
        env->DeleteGlobalRef(g_context);
        g_context = nullptr;
    }
}
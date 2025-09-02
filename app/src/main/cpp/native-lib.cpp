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
 * @brief Initialize native state and cache global references required by the native service.
 *
 * Stores the JavaVM in the global g_vm for later use and, if a non-null Android Context
 * is provided, creates a global reference to it and stores it in g_context so the native
 * code can access the Context across JNI calls. If obtaining the JavaVM or creating the
 * global reference fails, the function logs an error and returns early.
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
 * @brief Process a request string from Java and return a JSON-like response.
 *
 * Converts the provided Java UTF-8 string into a native C string, performs
 * processing (placeholder implementation), and returns a new Java string with
 * the response.
 *
 * If `request` is null or cannot be converted, the function returns a JSON-like
 * error string (e.g. "{'error':'Invalid request'}" or
 * "{'error':'Failed to process request'}").
 *
 * @param request UTF-8 encoded request payload from Java (expected to be a JSON-like string).
 * @return jstring A newly created Java string containing the response or an error object.
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
 * @brief Shut down the native CascadeAIService and release retained JNI resources.
 *
 * Deletes the stored global Android Context reference (if any) and clears the global
 * g_context pointer. Intended to be called from Java when the native service is no longer needed.
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
 * @brief JNI library load handler.
 *
 * Called when the native library is loaded by the JVM. Attempts to obtain a
 * JNIEnv for JNI version 1.6 and stores the provided JavaVM in the global
 * g_vm for later use by native code.
 *
 * @param vm Pointer to the JavaVM provided by the JVM.
 * @param reserved Reserved for future use (ignored).
 * @return JNI_VERSION_1_6 on success; JNI_ERR if a JNIEnv for the requested
 * JNI version could not be obtained.
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
 * @brief Called by the JVM when the native library is unloaded; cleans up JNI global state.
 *
 * Retrieves a JNIEnv for JNI version 1.6 and, if available, releases and clears the global
 * reference to the Android Context (g_context) to avoid leaks. If the environment cannot be
 * acquired, the function returns without making changes.
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
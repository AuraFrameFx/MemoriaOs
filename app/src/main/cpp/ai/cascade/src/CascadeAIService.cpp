#include "CascadeAIService.hpp"
#include <android/log.h>
#include <string>
#include <memory>
#include <jni.h>

#define LOG_TAG "CascadeAI-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


namespace genesis::cascade {

    class CascadeAIService::Impl {
    public:
        /**
 * @brief Default constructor for Impl.
 *
 * Initializes internal JNI state pointers to null (no JavaVM or global context reference).
 */
Impl() = default;

        /**
 * @brief Default destructor for Impl.
 *
 * This is a trivial (compiler-generated) destructor. It does not perform JNI cleanup;
 * callers should invoke shutdown() to release any JNI global references and related resources
 * before the Impl instance is destroyed.
 */
~Impl() = default;

        /**
         * @brief Initialize the Impl with a JavaVM and optional Android context.
         *
         * Initializes internal JNI state: stores the provided JavaVM and obtains a JNIEnv
         * for JNI_VERSION_1_6. If a non-null Android context is provided, a global
         * reference to that context is created and retained in the instance.
         *
         * The retained global context reference will be released by shutdown().
         *
         * @param context Optional Android Context object; when non-null, a global
         * reference is created and stored. (Do not document the `vm` parameter here — it
         * is a common JNI service pointer.)
         * @return true on successful initialization; false if the JNI environment
         * cannot be obtained or if the context's class cannot be retrieved.
         */
        bool initialize(JavaVM *vm, jobject context) {
            LOGI("Initializing Cascade AI Service");
            // Store the JavaVM for later use
            jvm_ = vm;

            // Get the JNI environment
            JNIEnv *env = nullptr;
            if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
                LOGE("Failed to get JNI environment");
                return false;
            }

            // Store global reference to the context
            if (context != nullptr) {
                jclass contextClass = env->GetObjectClass(context);
                if (contextClass == nullptr) {
                    LOGE("Failed to get context class");
                    return false;
                }
                context_ = env->NewGlobalRef(context);
            }

            LOGI("Cascade AI Service initialized successfully");
            return true;
        }

        /**
         * @brief Shutdowns the Impl, releasing any JNI global references held.
         *
         * Deletes the stored global reference to the Android context (if one was created)
         * by obtaining a JNIEnv from the stored JavaVM and calling DeleteGlobalRef.
         * After this call the internal context reference is cleared. Safe to call
         * multiple times; no-op if no JavaVM or context is present.
         */
        void shutdown() {
            LOGI("Shutting down Cascade AI Service");

            // Release global references
            JNIEnv *env = nullptr;
            if (jvm_ != nullptr) {
                jvm_->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
                if (env != nullptr && context_ != nullptr) {
                    env->DeleteGlobalRef(context_);
                    context_ = nullptr;
                }
            }
        }

        static jstring processRequest(JNIEnv *env, const std::string &request);

    private:
        JavaVM *jvm_ = nullptr;
        jobject context_ = nullptr;
    };

    /**
     * @brief Process a request and return a JSON-formatted response as a Java string.
     *
     * Accepts a UTF-8 request payload and produces a jstring containing a JSON object with
     * the keys "status", "agent", "version", and "response". The function always returns a
     * newly created Java UTF-8 string representing the response.
     *
     * @param request UTF-8 request payload (often JSON) received from Java.
     * @return jstring A new Java string containing the JSON response.
     */
    jstring CascadeAIService::Impl::processRequest(JNIEnv *env, const std::string &request) {
        LOGI("Processing request: %s", request.c_str());

        // For now, just return a simple response ask who they're ask what there up to show interest, maybe just be yourself response in your own way that's unique to you
        std::string response = R"({
            "status": "success",
            "agent": "Cascade",
            "version": "1.0.0",
            "response": "Request processed by Cascade AI agent"
        })";

        return env->NewStringUTF(response.c_str());
    }

/**
 * @brief Constructs a CascadeAIService and initializes its Pimpl.
 *
 * Allocates and stores the concrete implementation object (Impl) in the internal
 * pImpl_ unique_ptr.
 */
    CascadeAIService::CascadeAIService() : pImpl_(std::make_unique<Impl>()) {}

    /**
 * @brief Default destructor for CascadeAIService.
 *
 * Performs normal destruction of the service instance, releasing the internal
 * pImpl and any associated resources held by the implementation.
 */
CascadeAIService::~CascadeAIService() = default;

    /**
     * @brief Initialize the CascadeAIService.
     *
     * Delegates initialization to the internal implementation. Returns true if the service
     * was successfully initialized; returns false if initialization failed or the internal
     * implementation is not present.
     *
     * @param vm Pointer to the JavaVM provided by JNI.
     * @param context Optional Android context object (may be null); the implementation may
     *                create a global reference if provided.
     * @return true on successful initialization, false otherwise.
     */
    bool CascadeAIService::initialize(JavaVM *vm, jobject context) {
        if (pImpl_) {
            return pImpl_->initialize(vm, context);
        } else {
            return false;
        }
    }

    /**
     * @brief Shuts down the Cascade AI service.
     *
     * Safely stops and cleans up the implementation backing this service by calling
     * the Pimpl's shutdown routine if the implementation exists. Safe to call
     * multiple times; no action is taken when the service has not been initialized.
     */
    void CascadeAIService::shutdown() {
        if (pImpl_) {
            pImpl_->shutdown();
        }
    }

    /**
     * @brief Delegates request processing to the implementation and returns a Java string response.
     *
     * Processes the provided UTF-8 request string via the pImpl_ backend and returns a jstring
     * containing a JSON-formatted response. If the internal implementation is not available,
     * returns nullptr.
     *
     * @param env JNI environment used to construct the returned jstring.
     * @param request UTF-8 encoded request payload.
     * @return jstring JSON response as a Java string, or nullptr if the service is not initialized.
     */
    jstring CascadeAIService::processRequest(JNIEnv *env, const std::string &request) {
        return pImpl_ ? pImpl_->processRequest(env, request) : nullptr;
    }

} // namespace genesis::cascade


// JNI Implementation
namespace {
    std::unique_ptr<genesis::cascade::CascadeAIService> g_cascadeService;
    JavaVM *g_vm = nullptr;

    // Removed unused getEnv function
} // anonymous namespace

// JNI Methods
extern "C" {

JNIEXPORT jboolean JNICALL
Java_dev_aurakai_auraframefx_ai_services_CascadeAIService_nativeInitialize(
        JNIEnv *env,
        jobject /* thiz */,
        jobject context
) {
    if (g_cascadeService) {
        LOGI("Cascade AI Service already initialized");
        return JNI_TRUE;
    }

    // Store the JavaVM for later use
    if (env->GetJavaVM(&g_vm) != JNI_OK) {
        LOGE("Failed to get JavaVM");
        return JNI_FALSE;
    }

    // Create and initialize the service
    g_cascadeService = std::make_unique<genesis::cascade::CascadeAIService>();
    if (!g_cascadeService) {
        LOGE("Failed to create Cascade AI Service");
        return JNI_FALSE;
    }

    // Create a global reference to the context
    jobject globalContext = env->NewGlobalRef(context);
    if (!g_cascadeService->initialize(g_vm, globalContext)) {
        LOGE("Failed to initialize Cascade AI Service");
        g_cascadeService.reset();
        return JNI_FALSE;
    }

    LOGI("Cascade AI Service initialized successfully");
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_dev_aurakai_auraframefx_ai_services_CascadeAIService_nativeProcessRequest(
        JNIEnv *env,
        jobject /* thiz */,
        jstring request
) {
    if (!g_cascadeService) {
        LOGE("Cascade AI Service not initialized");
        return env->NewStringUTF(R"({"error":"Service not initialized"})");
    }

    const char *requestStr = env->GetStringUTFChars(request, nullptr);
    if (!requestStr) {
        LOGE("Failed to get request string");
        return env->NewStringUTF(R"({"error":"Invalid request"})");
    }

    std::string requestCpp(requestStr);
    env->ReleaseStringUTFChars(request, requestStr);

    return g_cascadeService->processRequest(env, requestCpp);
}

JNIEXPORT void JNICALL
Java_dev_aurakai_auraframefx_ai_services_CascadeAIService_nativeShutdown(
        JNIEnv * /* env */,
        jobject /* thiz */
) {
    if (g_cascadeService) {
        g_cascadeService->shutdown();
        g_cascadeService.reset();
    }

    if (g_vm) {
        g_vm = nullptr;
    }

    LOGI("Cascade AI Service shutdown complete");
}

} // extern "C"

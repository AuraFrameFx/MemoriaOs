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
 * @brief Default constructor.
 *
 * Constructs an Impl with internal JNI handles default-initialized (no JavaVM or context set).
 */
Impl() = default;

        /**
 * @brief Default destructor for Impl.
 *
 * Does not perform JNI cleanup. Call shutdown() to release any JavaVM pointers or global JNI
 * references (such as the stored Android context) before destroying the object.
 */
~Impl() = default;

        /**
         * @brief Initialize the implementation with a Java VM and optional Android context.
         *
         * Caches the provided JavaVM for later JNI use and, if a non-null Android context
         * is provided, creates and stores a global reference to that context for use
         * across threads/calls.
         *
         * @param context Android Context object to retain as a global reference; may be nullptr.
         * @return true if the JavaVM was cached and (when provided) the context global reference was created successfully.
         * @return false if obtaining a JNIEnv from the JavaVM fails or if the provided context's class cannot be retrieved.
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
         * @brief Shut down the implementation and release JNI global resources.
         *
         * Obtains a JNIEnv from the stored JavaVM (if present) and deletes the cached
         * global reference to the Android context (if one was stored). Safe to call
         * when not initialized; no action is taken if there is no JavaVM or no stored
         * context reference.
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
     * @brief Build a JSON response for the given request and return it as a Java string.
     *
     * Currently returns a fixed JSON object indicating success and basic agent
     * metadata; intended as a placeholder for real request-processing logic.
     *
     * @param request Incoming request payload as a UTF-8 C++ string.
     * @return jstring A newly allocated JNI string containing the JSON response.
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
 * @brief Constructs a CascadeAIService and initializes its PImpl.
 *
 * Allocates the internal Impl instance (PImpl) that holds JNI state and implements the service behavior.
 */
    CascadeAIService::CascadeAIService() : pImpl_(std::make_unique<Impl>()) {}

    /**
 * @brief Destroys the CascadeAIService instance.
 *
 * Cleans up the service by destroying its Pimpl (internal implementation) and releasing associated resources.
 */
CascadeAIService::~CascadeAIService() = default;

    /**
     * @brief Initialize the CascadeAIService by delegating to the implementation.
     *
     * Attempts to initialize the underlying implementation with the given Java VM and Android
     * context. If the internal implementation pointer is null, initialization is not performed.
     *
     * @param vm Pointer to the JVM to be stored for JNI interactions; may be nullptr to indicate no JVM.
     * @param context Local or global reference to an Android Context object; may be null.
     * @return true if the implementation exists and initialization succeeded; false otherwise.
     */
    bool CascadeAIService::initialize(JavaVM *vm, jobject context) {
        if (pImpl_) {
            return pImpl_->initialize(vm, context);
        } else {
            return false;
        }
    }

    /**
     * @brief Shutdowns the Cascade AI service.
     *
     * Safely shuts down the underlying implementation if present. If the service
     * was not initialized (no implementation instance), this call is a no-op.
     * Delegates cleanup work to the pImpl_->shutdown() implementation.
     */
    void CascadeAIService::shutdown() {
        if (pImpl_) {
            pImpl_->shutdown();
        }
    }

    /**
     * @brief Process an AI request and return a Java string response.
     *
     * Processes the provided request payload and returns a JNI `jstring` containing a JSON-formatted response.
     *
     * @param request UTF-8 request payload (expected JSON).
     * @return jstring Java string with the JSON response, or `nullptr` if the underlying service is not initialized.
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

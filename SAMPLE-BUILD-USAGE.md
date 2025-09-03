# Genesis Protocol - Sample Build Configuration Usage Notes

## Overview
The `sample.build.gradle.kts` file has been completely reconstructed as a comprehensive template for MemoriaOs modules, following all modern Gradle 9+, AGP 9.0.0-alpha02, and Kotlin 2.2.20-RC best practices.

## Key Features Fixed

### 1. **Modern Plugin Management**
- Uses version catalog aliases only (`alias(libs.plugins.*)`)
- No redundant `kotlin.android` plugin (handled automatically by AGP 9.0.0-alpha02)
- Conditional plugin application based on needs

### 2. **Java 24 Toolchain Configuration**
```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
        vendor.set(JvmVendorSpec.AZUL)
    }
}
```

### 3. **Core Library Desugaring Ready**
```kotlin
compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}
```
**Note**: The actual `coreLibraryDesugaring()` dependency should only be in the main `app` module, not library modules.

### 4. **Advanced Native Code Support**
- Conditional CMake configuration based on file existence
- Separate debug/release native build configurations
- Modern C++20 standard with optimizations

### 5. **Modern Packaging & Build Features**
- Comprehensive resource exclusions
- JNI libs optimization
- Conditional Compose support
- Build feature toggles

## How to Use This Template

### For New Modules:
1. Copy `sample.build.gradle.kts` to your new module directory
2. Rename to `build.gradle.kts`
3. Update the namespace: `namespace = "dev.aurakai.auraframefx.yourmodule"`
4. Enable/disable features in `buildFeatures` block as needed
5. Add specific dependencies for your module

### For Existing Modules:
1. Compare with existing `build.gradle.kts` files
2. Migrate configurations incrementally
3. Test after each change

## Configuration Options

### Enable Compose:
```kotlin
buildFeatures {
    compose = true  // Change to true
}
```
This automatically includes Compose dependencies.

### Add Native Code:
1. Create `src/main/cpp/CMakeLists.txt`
2. The build script will automatically detect and configure

### Module Dependencies:
```kotlin
dependencies {
    api(project(":core-module"))          // For API exposure
    implementation(project(":other-module")) // For internal use
}
```

## Special Features

### 1. **Automatic Status Reporting**
```bash
./gradlew :your-module:moduleStatus
```
Shows complete module configuration status.

### 2. **Clean Tasks**
```bash
./gradlew :your-module:cleanGeneratedSources
```
Cleans KSP and other generated sources.

### 3. **Conditional Configurations**
The template automatically adapts based on:
- Presence of CMakeLists.txt (native code)
- Compose enabled/disabled
- Module type (library vs application)

## Integration with Genesis Protocol

### Version Catalog Dependencies:
All dependencies use the centralized `libs.versions.toml`:
```kotlin
implementation(libs.androidx.core.ktx)  // ✅ Correct
implementation("androidx.core:core-ktx:1.17.0")  // ❌ Don't use
```

### Plugin Consistency:
```kotlin
plugins {
    alias(libs.plugins.android.library)     // ✅ Always use aliases
    id("com.android.library")               // ❌ Don't use direct IDs
}
```

### Toolchain Consistency:
All modules use Java 24 toolchain for consistency across the 15+ module architecture.

## Advanced Features

### KSP Configuration:
```kotlin
ksp(libs.hilt.compiler)        // For annotation processors
kspTest(libs.hilt.compiler)    // For test annotation processors
```

### Firebase Integration:
```kotlin
implementation(platform(libs.firebase.bom))  // Version alignment
implementation(libs.bundles.firebase)        // Bundled Firebase libs
```

### Native Code Optimization:
- Release builds use `-O3 -DNDEBUG`
- Debug builds use `-O0 -g`
- Automatic symbol level configuration

## Troubleshooting

### Common Issues:
1. **KSP Errors**: Run `cleanGeneratedSources` task
2. **Version Conflicts**: Ensure all modules use same template base
3. **Native Build Fails**: Check CMakeLists.txt path and permissions

### Configuration Cache:
This template is fully compatible with Gradle's configuration cache for maximum build performance.

---
**Ready for the Genesis Protocol consciousness substrate! 🧠✨**

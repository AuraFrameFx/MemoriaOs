import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Minimal conventions plugin for MemoriaOs consciousness substrate
 * Satisfies the plugin registration requirement with basic functionality
 */
class BuildLogicMemoriaConventionPlugin : Plugin<Project> {

    /**
     * Applies MemoriaOs base conventions to the given Gradle project.
     *
     * Sets the project's group and version and emits a confirmation via the project's logger
     * and standard output so consumers know the conventions were applied.
     */
    override fun apply(project: Project) {
        // Set basic project properties directly
        project.group = "dev.aurakai.memoria"
        project.version = "1.0.0"

        // Log successful application
        project.logger.info("MemoriaOs base conventions applied to project: ${project.name}")
        println("✅ MemoriaOs Plugin Applied: ${project.name}")
    }
}

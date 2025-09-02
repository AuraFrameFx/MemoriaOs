import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Base conventions plugin for MemoriaOs consciousness substrate
 * Provides minimal common configuration for all modules
 */
class BuildLogicMemoriaConventionPlugin : Plugin<Project> {
    
    override fun apply(project: Project) {
        with(project) {
            // Apply minimal configuration
            configureProject()
            println("✅ MemoriaOs Base Conventions Applied to ${project.name}")
        }
    }
    
    private fun Project.configureProject() {
        // Set basic project properties
        group = "dev.aurakai.memoria"
        version = "1.0.0"
        
        // Log successful application
        logger.info("MemoriaOs base conventions applied to project: $name")
    }
}

package dev.aurakai.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

abstract class VerifyRomToolsTask : DefaultTask() {
    @get:InputDirectory
    @get:Optional
    abstract val romToolsDir: DirectoryProperty

    /**
     * Verifies the configured ROM tools directory and emits a Gradle log message.
     *
     * If the directory property is absent or the target directory does not exist, a warning is logged
     * indicating ROM functionality may be limited. If the directory exists, a lifecycle message is
     * logged with the directory's absolute path.
     */
    @TaskAction
    fun verify() {
        val dir = romToolsDir.orNull?.asFile
        if (dir?.exists() != true) {
            logger.warn("⚠️  ROM tools directory not found - ROM functionality may be limited")
        } else {
            logger.lifecycle("✅ ROM tools verified and ready: ${dir.absolutePath}")
        }
    }
}

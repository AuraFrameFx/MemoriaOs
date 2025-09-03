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
     * Verifies the optional ROM tools directory and reports its status.
     *
     * If the configured `romToolsDir` is missing or does not exist, logs a warning indicating ROM
     * functionality may be limited. If the directory exists, logs a lifecycle message with its
     * absolute path.
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

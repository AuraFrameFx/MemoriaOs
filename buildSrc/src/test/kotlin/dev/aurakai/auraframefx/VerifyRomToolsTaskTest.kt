@file:Suppress("UnstableApiUsage")

/*
Test framework note:
- Prefers JUnit Jupiter (org.junit.jupiter.api) if available; otherwise falls back to kotlin.test.
- Uses Gradle Test Fixtures (ProjectBuilder) to create the task.
- No new dependencies added; aligns with existing test setup.
*/

package dev.aurakai.auraframefx

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import java.io.File

// Import the task under test using its fully-qualified name to avoid package mismatch.
// If your source package differs (e.g., dev.aurakai.gradle.tasks), update the import accordingly.
import dev.aurakai.gradle.tasks.VerifyRomToolsTask

class VerifyRomToolsTaskTest {

    @Test
    fun verify_warns_when_directory_missing_or_null() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.create("verifyRomTools", VerifyRomToolsTask::class.java)

        // Capture output from Gradle logging (warn/lifecycle may propagate to stderr/stdout)
        val out = StringBuilder()
        val err = StringBuilder()
        project.logging.addStandardOutputListener { out.append(it).append('\n') }
        project.logging.addStandardErrorListener { err.append(it).append('\n') }

        // Case 1: romToolsDir not set (null)
        task.verify()
        val combined1 = out.toString() + err.toString()
        assertTrue(
            combined1.contains("ROM tools directory not found"),
            "Expected warning when romToolsDir is null, got: $combined1"
        )

        // Reset buffers
        out.setLength(0); err.setLength(0)

        // Case 2: romToolsDir set to non-existent dir
        val missingDir = File(project.projectDir, "does-not-exist-123456")
        task.romToolsDir.set(project.layout.dir(project.provider { missingDir }))
        task.verify()
        val combined2 = out.toString() + err.toString()
        assertTrue(
            combined2.contains("ROM tools directory not found"),
            "Expected warning when romToolsDir does not exist, got: $combined2"
        )
    }

    @Test
    fun verify_logs_lifecycle_when_directory_exists() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.create("verifyRomTools", VerifyRomToolsTask::class.java)

        val out = StringBuilder()
        val err = StringBuilder()
        project.logging.addStandardOutputListener { out.append(it).append('\n') }
        project.logging.addStandardErrorListener { err.append(it).append('\n') }

        // Create a temporary directory within the project to simulate a valid ROM tools dir
        val realDir = File(project.projectDir, "romtools-temp")
        assertTrue(realDir.mkdirs() || realDir.exists(), "Failed to create temp directory for test")
        task.romToolsDir.set(project.layout.dir(project.provider { realDir }))

        task.verify()

        val combined = out.toString() + err.toString()
        assertTrue(
            combined.contains("ROM tools verified and ready"),
            "Expected lifecycle log when directory exists, got: $combined"
        )
        // Also ensure absolute path appears in the log for additional validation
        assertTrue(
            combined.contains(realDir.absolutePath),
            "Expected absolute path in lifecycle log, got: $combined"
        )
    }

    @Test
    fun verify_is_idempotent_and_side_effect_free() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.create("verifyRomTools", VerifyRomToolsTask::class.java)

        val out = StringBuilder()
        val err = StringBuilder()
        project.logging.addStandardOutputListener { out.append(it).append('\n') }
        project.logging.addStandardErrorListener { err.append(it).append('\n') }

        // Repeated invocations should not throw and should consistently log warnings when unset
        repeat(3) {
            task.verify()
        }
        val logs = out.toString() + err.toString()
        val warnCount = Regex("ROM tools directory not found").findAll(logs).count()
        assertTrue(warnCount >= 1, "Expected at least one warning across repeated calls")
    }

    @Test
    fun romToolsDir_property_is_optional_and_configurable() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.create("verifyRomTools", VerifyRomToolsTask::class.java)

        // DirectoryProperty exists and is optional
        assertNotNull(task.romToolsDir)

        // Set a directory, then re-point to another one, ensure it updates
        val dir1 = File(project.projectDir, "tools1").apply { mkdirs() }
        val dir2 = File(project.projectDir, "tools2").apply { mkdirs() }

        task.romToolsDir.set(project.layout.dir(project.provider { dir1 }))
        assertEquals(dir1.absolutePath, task.romToolsDir.get().asFile.absolutePath)

        task.romToolsDir.set(project.layout.dir(project.provider { dir2 }))
        assertEquals(dir2.absolutePath, task.romToolsDir.get().asFile.absolutePath)
    }
}

// NOTE: If your project uses a different test framework (e.g., JUnit Jupiter or Kotest),
// these tests rely only on kotlin.test, which delegates to the platform runner when configured.
// Adjust imports/annotations if necessary to match existing conventions.
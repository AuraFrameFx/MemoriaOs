// ==== GENESIS PROTOCOL - KOVER CONVENTION ====
// Code coverage configuration for Genesis modules

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")
            
            // Configure Kover for Genesis modules
            extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
                reports {
                    total {
                        html {
                            onCheck = false
                            htmlDir = layout.buildDirectory.dir("reports/kover/html")
                        }
                        
                        xml {
                            onCheck = false
                            xmlFile = layout.buildDirectory.file("reports/kover/coverage.xml")
                        }
                        
                        verify {
                            onCheck = true
                            rule {
                                isEnabled = true
                                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION
                                
                                filters {
                                    excludes {
                                        classes(
                                            "*Fragment",
                                            "*Fragment\$*",
                                            "*Activity",
                                            "*Activity\$*",
                                            "*.databinding.*",
                                            "*.BuildConfig",
                                            "*ComposableSingletons*",
                                            "*_Factory*",
                                            "*_MembersInjector*",
                                            "*_Provide*Factory*",
                                            "*Module_*",
                                            "dagger.hilt.*",
                                            "hilt_aggregated_deps.*",
                                            "*_HiltModules*",
                                            "*Hilt_*"
                                        )
                                        packages(
                                            "dagger.hilt.internal.aggregatedroot.codegen",
                                            "dagger.hilt.internal.processingroot.codegen",
                                            "hilt_aggregated_deps"
                                        )
                                    }
                                }
                                
                                bound {
                                    minValue = 80 // 80% coverage requirement
                                    maxValue = 100
                                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE
                                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

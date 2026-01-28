@file:Suppress("NOTHING_TO_INLINE")

package net.frozenblock.configurableeverything.scripting.util

import com.hypixel.hytale.assetstore.JsonAsset
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec
import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.lookup.StringCodecMapCodec
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.PluginBase
import com.hypixel.hytale.server.core.plugin.PluginClassLoader
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry
import net.frozenblock.configurableeverything.CEPlugin
import net.frozenblock.configurableeverything.util.*
import java.io.File
import kotlin.collections.set
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.impl.internalScriptingRunSuspend
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.loadDependencies
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.util.filterByAnnotationType

@KotlinScript(
    fileExtension = KOTLIN_SCRIPT_EXTENSION,
    compilationConfiguration = CEScriptCompilationConfig::class,
    evaluationConfiguration = CEScriptEvaluationConfig::class,
)
abstract class ClientCEScript : CEScript() {

    override fun runEachTick(tickFun: () -> Unit) {
        super.runEachTick(tickFun)
    }
}

@KotlinScript(
    fileExtension = KOTLIN_SCRIPT_EXTENSION,
    compilationConfiguration = CEScriptCompilationConfig::class,
    evaluationConfiguration = CEScriptEvaluationConfig::class,
)
// dont use environment annotations anywhere
abstract class CEScript {
    companion object {
        @PublishedApi
        internal var POST_RUN_FUNS: MutableMap<Int, () -> Unit>? = mutableMapOf()
    }

    /**
     * The name of the script file.
     */
    @JvmField
    val scriptName: String = this::class.java.simpleName.let { name -> name.substring(0, name.length - 5) }

    @JvmField
    val LOGGER: HytaleLogger = HytaleLogger.get("CE Script: $scriptName")

    val CE_PLUGIN: CEPlugin = CEPlugin.INSTANCE

    inline fun runLate(priority: Int, noinline `fun`: () -> Unit) {
        POST_RUN_FUNS!![priority] = `fun`
    }

    val classLoader: PluginClassLoader = CE_PLUGIN.classLoader

    val METRICS_REGISTRY = PluginBase.METRICS_REGISTRY

    val dataDirectory = CE_PLUGIN.dataDirectory

    val clientFeatureRegistry = CE_PLUGIN.clientFeatureRegistry

    val commandRegistry = CE_PLUGIN.commandRegistry

    val eventRegistry = CE_PLUGIN.eventRegistry

    val blockStateRegistry = CE_PLUGIN.blockStateRegistry

    val entityRegistry = CE_PLUGIN.entityRegistry

    val taskRegistry = CE_PLUGIN.taskRegistry

    val entityStoreRegistry = CE_PLUGIN.entityStoreRegistry

    val chunkStoreRegistry = CE_PLUGIN.chunkStoreRegistry

    fun <T, C : Codec<out T>> getCodecRegistry(mapCodec: StringCodecMapCodec<T, C>): CodecMapRegistry<T, C>
        = CE_PLUGIN.getCodecRegistry(mapCodec)

    fun <K, T : JsonAsset<K>> getCodecRegistry(mapCodec: AssetCodecMapCodec<K, T>): CodecMapRegistry.Assets<T, *>
        = CE_PLUGIN.getCodecRegistry(mapCodec)

    val basePermission = CE_PLUGIN.basePermission

    open fun runEachTick(tickFun: () -> Unit) {
        runEachServerTick { tickFun() }
    }

    inline fun runEachServerTick(crossinline tickFun: (/*server*/) -> Unit) {
        //ServerTickEvents.START_SERVER_TICK.register { /*server*/ -> tickFun(/*server*/) }
    }

    inline fun log(message: Any?) = LOGGER.atInfo().log(message.toString())

    inline fun logWarning(message: Any?) = LOGGER.atWarning().log(message.toString())

    inline fun logError(message: Any?, e: Throwable?) = LOGGER.atSevere().log(message.toString(), e)
}

open class CEScriptCompilationConfig internal constructor() : ScriptCompilationConfiguration({
    val defaultImports = buildList {
        this.addAll(
            listOf(
                // who even needs to take these away, right?
                "net.frozenblock.configurableeverything.scripting.util.*",
                "net.frozenblock.configurableeverything.scripting.util.api.*",
                "net.frozenblock.configurableeverything.scripting.util.api.conversion.*",
                "net.frozenblock.lib.config.api.instance.xjs.XjsOps",
                "com.mojang.serialization.JsonOps",
                "net.frozenblock.lib.config.api.instance.json.JanksonOps",
                "net.frozenblock.lib.config.api.instance.ConfigModification",

                "net.minecraft.core.*",
                "net.minecraft.core.registries.*",
                "net.minecraft.resources.ResourceKey",
                "net.minecraft.resources.Identifier",
                "net.minecraft.world.level.block.Block",
                "net.minecraft.world.level.block.Blocks",
                "net.minecraft.world.level.block.state.BlockBehaviour",
                "net.minecraft.world.level.block.state.BlockBehaviour.Properties",
                "net.minecraft.world.level.dimension.DimensionType"
            )
        )
        //this.addAll(ScriptingConfig.get().defaultImports)
    }
    defaultImports(defaultImports)
    defaultImports(
        DependsOn::class,
        Repository::class,
        Import::class,
        CompilerOptions::class,
    )
    baseClass(CEScript::class)
    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
    jvm {
        // the dependenciesFromCurrentContext helper function extracts the classpath from current thread classloader
        // and take jars with mentioned names to the compilation classpath via `dependencies` key.

        // Adds the remapped Minecraft and mod jars to the classpath
        //if (ScriptingConfig.get().remapping)
        //    updateClasspath(REMAPPED_SOURCES_CACHE.asFileList!!)
        updateClasspath(listOf(CEPlugin.HYTALE_JAR))

        dependenciesFromCurrentContext(wholeClasspath = true)
    }

    compilerOptions(listOf(
        "-jvm-target", "25",
        //"-language-version", "2.0",
    ))
    compilerOptions.append("-Xadd-modules=ALL-MODULE-PATH")

    refineConfiguration {
        // the callback called when any of the listed file-level annotations are encountered in the compiled script
        // the processing is defined by the `handler`, that may return refined configuration depending on the annotations
        onAnnotations(
            DependsOn::class,
            Repository::class,
            Import::class,
            CompilerOptions::class,
            handler = ::configureDepsOnAnnotations
        )
    }
}) {
    // used for serialization for some reason
    private fun readResolve(): Any = CEScriptCompilationConfig()
}

object CEScriptEvaluationConfig : ScriptEvaluationConfiguration({
    jvm {
        loadDependencies(true)
        scriptsInstancesSharing(true)
    }
}) {
    // used for serialization for some reason
    private fun readResolve(): Any = CEScriptEvaluationConfig
}

private val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())

fun configureDepsOnAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    val diagnostics = arrayListOf<ScriptDiagnostic>()

    val annotations = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)?.takeIf { it.isNotEmpty() }
        ?: return context.compilationConfiguration.asSuccess()

    val scriptBaseDir = (context.script as? FileBasedScriptSource)?.file?.parentFile
    val importedSources = linkedMapOf<String, Pair<File, String>>()
    var hasImportErrors = false
    annotations.filterByAnnotationType<Import>().forEach { scriptAnnotation ->
        scriptAnnotation.annotation.paths.forEach { sourceName ->
            val file = (scriptBaseDir?.resolve(sourceName) ?: File(sourceName)).normalize()
            val keyPath = file.absolutePath
            val prevImport = importedSources.put(keyPath, file to sourceName)
            if (prevImport != null) {
                diagnostics.add(
                    ScriptDiagnostic(
                        ScriptDiagnostic.unspecifiedError, "Duplicate imports: \"${prevImport.second}\" and \"$sourceName\"",
                        sourcePath = context.script.locationId, location = scriptAnnotation.location?.locationInText
                    )
                )
                hasImportErrors = true
            }
        }
    }
    if (hasImportErrors) return ResultWithDiagnostics.Failure(diagnostics)

    val compileOptions = annotations.filterByAnnotationType<CompilerOptions>().flatMap {
        it.annotation.options.toList()
    }

    val resolveResult = try {
        @Suppress("DEPRECATION_ERROR")
        internalScriptingRunSuspend {
            resolver.resolveFromScriptSourceAnnotations(annotations.filter { it.annotation is DependsOn || it.annotation is Repository })
        }
    } catch (e: Throwable) {
        diagnostics.add(e.asDiagnostics(path = context.script.locationId))
        ResultWithDiagnostics.Failure(diagnostics)
    }

    return resolveResult.onSuccess { resolvedClasspath ->
        ScriptCompilationConfiguration(context.compilationConfiguration) {
            updateClasspath(resolvedClasspath)
            if (importedSources.isNotEmpty()) {
                importScripts.append(importedSources.values.map { FileScriptSource(it.first) })
            }
            if (compileOptions.isNotEmpty()) compilerOptions.append(compileOptions)
        }.asSuccess()
    }
}

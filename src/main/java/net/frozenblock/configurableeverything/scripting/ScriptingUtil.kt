package net.frozenblock.configurableeverything.scripting.util

import kotlinx.coroutines.runBlocking
import net.frozenblock.configurableeverything.util.*
import java.io.File
import java.nio.file.Path
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvmhost.BasicJvmScriptJarGenerator
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.loadScriptFromJar

private fun ResultWithDiagnostics<*>.logReports() {
    this.reports.forEach {
        val message = " : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}"
        when (it.severity) {
            ScriptDiagnostic.Severity.DEBUG -> logDebug(message)
            ScriptDiagnostic.Severity.INFO -> log(message)
            ScriptDiagnostic.Severity.WARNING -> logWarn(message)
            ScriptDiagnostic.Severity.ERROR -> logError(message, it.exception)
            ScriptDiagnostic.Severity.FATAL -> logError(message, it.exception)
            else -> logError(message)
        }
    }
}

internal object ScriptingUtil {

    private var COMPILED_SCRIPTS: MutableList<String>? = mutableListOf()
    private val SCRIPTS_TO_EVAL: MutableMap<CompiledScript, File> = mutableMapOf()

    private suspend fun compileScript(script: File, addToBuffer: Boolean = true): Pair<CompiledScript, File>? {
        if (COMPILED_SCRIPTS?.contains(script.path) == true) return null
        COMPILED_SCRIPTS?.add(script.path)

        val compilationConfiguration = CEScriptCompilationConfig()
        val evaluationConfiguration = CEScriptEvaluationConfig
        val compiledScript: KJvmCompiledScript = JvmScriptCompiler()(
            script.toScriptSource(),
            compilationConfiguration
        ).apply { this.logReports() }.valueOrNull() as? KJvmCompiledScript ?: error("Compiled script is not java or is null")
        if (addToBuffer) SCRIPTS_TO_EVAL[compiledScript] = script
        return Pair(compiledScript, script)
    }

    fun runScripts() {
        log("Running scripts")
        compileScripts(KOTLIN_SCRIPT_PATH)
        COMPILED_SCRIPTS?.clear(); COMPILED_SCRIPTS = null

        runBlocking { evalScripts() }

        CEScript.POST_RUN_FUNS?.apply {
            this.toSortedMap().forEach { (_, value) ->
                value.invoke() // make sure to not use coroutines here
            }
        }
        CEScript.POST_RUN_FUNS = null
    }

    private fun compileScripts(path: Path) {
        val folder = path.toFile().listFiles() ?: return
        for (file in folder) {
            if (file.isDirectory) continue
            try {
                runBlocking { compileScript(file) }
            } catch (e: Exception) {
                logError("Error while compiling script $file", e)
            }
        }
    }

    @Throws(Exception::class)
    internal suspend fun forceRunScript(file: File) {
        val (compiledScript, compiledFile) = runBlocking { compileScript(file, addToBuffer = false) }
            ?: throw Exception("Unable to compile script $file")
        evalScript(compiledScript, compiledFile)
    }

    private suspend fun evalScript(script: CompiledScript, file: File) {
        try {
            val result = BasicJvmScriptEvaluator()(script, CEScriptEvaluationConfig)
            result.logReports()
            val returnValue = result.valueOrNull()?.returnValue
            if (returnValue is ResultValue.Error) {
                logError("Script $file threw an error during execution", returnValue.error)
            }
        } catch (e: Throwable) {
            logError("Error while running script file $file", e)
        }
    }

    private suspend fun evalScripts() {
        for ((script, file) in SCRIPTS_TO_EVAL) {
            evalScript(script, file)
        }
        SCRIPTS_TO_EVAL.clear()
    }
}

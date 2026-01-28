package net.frozenblock.configurableeverything.util

import net.frozenblock.configurableeverything.CEPlugin
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists

const val KOTLIN_SCRIPT_EXTENSION = "cevt.kts"

val KOTLIN_SCRIPT_PATH: Path = CEPlugin.INSTANCE.dataDirectory.resolve("scripts/").apply {
    if (this.notExists())
        this.createDirectory()
}
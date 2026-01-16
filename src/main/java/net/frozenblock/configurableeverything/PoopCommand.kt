package net.frozenblock.configurableeverything

import com.hypixel.hytale.protocol.GameMode
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.AbstractCommand
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.ParseResult
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.CompletableFuture

internal class PoopCommand : AbstractCommand("poop", "poop") {
    private val argument: OptionalArg<String>

    init {
        val arg: SingleArgumentType<String> = object : SingleArgumentType<String>(
            "Default Search",
            "Opens the screen with this text in the search field",
            "iron",
            "stone"
        ) {
            override fun parse(s: String, parseResult: ParseResult): String {
                return s
            }
        }
        this.argument = this.withOptionalArg("message", "Message to print", arg)
        this.setPermissionGroup(GameMode.Adventure)
    }

    override fun execute(context: CommandContext): CompletableFuture<Void>? {
        val sender = context.senderAsPlayerRef()
        if (sender!!.isValid) {
            val store = sender.store
            val world = store.getExternalData().world
            return CompletableFuture.runAsync({
                val playerRefComponent = store.getComponent(sender, PlayerRef.getComponentType())
                var message = ""
                if (context.get(this.argument) != null) {
                    message = context.get(this.argument) + ' '
                }
                message += "poop"
                if (playerRefComponent != null) {
                    context.sendMessage(Message.raw(message))
                }
            }, world)
        }
        return CompletableFuture.completedFuture<Void>(null)
    }
}

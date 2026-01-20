package net.frozenblock.configurableeverything;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PoopCommand extends AbstractCommand {

    private final OptionalArg<String> argument;

    protected PoopCommand() {
        super("poop", "poop");

        var arg = new SingleArgumentType<String>(
            "Default Search",
            "Opens the screen with this text in the search field",
            "iron",
            "stone"
        ) {
            @Override
            public String parse(String s, ParseResult parseResult) {
                return s;
            }
        };
        this.argument = this.withOptionalArg("message", "Message to print", arg);
        this.setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected @Nullable CompletableFuture<Void> execute(@NonNull CommandContext context) {
        var sender = context.senderAsPlayerRef();
        if (sender.isValid()) {
            var store = sender.getStore();
            var world = store.getExternalData().getWorld();
            return CompletableFuture.runAsync(() -> {
                var playerRefComponent = store.getComponent(sender, PlayerRef.getComponentType());
                String message = "";
                if (context.get(this.argument) != null) {
                    message = context.get(this.argument) + ' ';
                }
                message += "poop";
                if (playerRefComponent != null) {
                    context.sendMessage(Message.raw(message));
                }
            }, world);
        }
        return CompletableFuture.completedFuture(null);
    }
}

package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import java.util.concurrent.ExecutionException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class BanCommand extends SubCommand {

    public BanCommand(final TebexPlugin platform) {
        super(platform, "ban", "tebex.ban");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final TebexPlugin platform = this.getPlatform();

        final String playerName = context.getArgument("playerName", String.class);
        String reason = "";
        String ip = "";

        try {
            reason = context.getArgument("reason", String.class);
            ip = context.getArgument("ip", String.class);
        } catch (final IllegalArgumentException ignored) {}

        if (!platform.isSetup()) {
            source.sendSystemMessage(Component.literal(
                    "§b[Tebex] §7This server is not connected to a webstore. Use /tebex secret to"
                            + " set your store key."));
            return;
        }

        try {
            final boolean success = platform.getSDK().createBan(playerName, ip, reason).get();
            if (success) {
                source.sendSystemMessage(
                        Component.literal("§b[Tebex] §7Player banned successfully."));
            } else {
                source.sendSystemMessage(Component.literal("§b[Tebex] §7Failed to ban player."));
            }
        } catch (final InterruptedException | ExecutionException e) {
            source.sendSystemMessage(
                    Component.literal("§b[Tebex] §7Error while banning player: " + e.getMessage()));
        }
    }

    @Override
    public String getDescription() {
        return "Bans a player from using the webstore. Unbans can only be made via the web panel.";
    }

    @Override
    public String getUsage() {
        return "<playerName>";
    }
}

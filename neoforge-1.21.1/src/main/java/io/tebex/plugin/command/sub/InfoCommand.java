package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class InfoCommand extends SubCommand {

    public InfoCommand(final TebexPlugin platform) {
        super(platform, "info", "tebex.info");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final TebexPlugin platform = this.getPlatform();

        if (platform.isSetup()) {
            source.sendSystemMessage(Component.literal("§b[Tebex] §7Information for this server:"));
            source.sendSystemMessage(Component.literal(
                    "§b[Tebex] §7" + platform.getStoreInformation().getServer().getName()
                            + " for webstore " + platform.getStoreInformation().getStore()
                            .getName()));
            source.sendSystemMessage(Component.literal(
                    "§b[Tebex] §7Server prices are in " + platform.getStoreInformation().getStore()
                            .getCurrency().getIso4217()));
            source.sendSystemMessage(Component.literal(
                    "§b[Tebex] §7Webstore domain " + platform.getStoreInformation().getStore()
                            .getDomain()));
        } else {
            source.sendSystemMessage(Component.literal(
                    "§b[Tebex] §7This server is not connected to a webstore. Use /tebex secret to"
                            + " set your store key."));
        }
    }

    @Override
    public String getDescription() {
        return "Gets information about this server's connected store.";
    }
}

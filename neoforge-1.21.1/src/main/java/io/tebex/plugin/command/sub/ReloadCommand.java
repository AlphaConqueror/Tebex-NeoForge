package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ReloadCommand extends SubCommand {

    public ReloadCommand(final TebexPlugin platform) {
        super(platform, "reload", "tebex.reload");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();

        final TebexPlugin platform = this.getPlatform();
        try {
            final YamlDocument configYaml = platform.initPlatformConfig();
            platform.loadServerPlatformConfig(configYaml);
            platform.refreshListings();
            //            platform.setBuyGUI(new BuyGUI(platform));
            platform.getSDK().sendPluginEvents();

            source.sendSystemMessage(Component.literal("§8[Tebex] §7Successfully reloaded."));
        } catch (final IOException e) {
            source.sendSystemMessage(
                    Component.literal("§8[Tebex] §cFailed to reload the plugin: Check Console."));
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin.";
    }
}

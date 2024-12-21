package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.sdk.SDK;
import io.tebex.sdk.exception.ServerNotFoundException;
import io.tebex.sdk.platform.config.ServerPlatformConfig;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class SecretCommand extends SubCommand {

    public SecretCommand(final TebexPlugin platform) {
        super(platform, "secret", "tebex.secret");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();

        final String serverToken = context.getArgument("key", String.class);
        final TebexPlugin platform = this.getPlatform();

        final SDK analyse = platform.getSDK();
        final ServerPlatformConfig analyseConfig = platform.getPlatformConfig();
        final YamlDocument configFile = analyseConfig.getYamlDocument();

        analyse.setSecretKey(serverToken);

        platform.getSDK().getServerInformation().thenAccept(serverInformation -> {
            analyseConfig.setSecretKey(serverToken);
            configFile.set("server.secret-key", serverToken);

            try {
                configFile.save();
            } catch (final IOException e) {
                source.sendSystemMessage(
                        Component.literal("§b[Tebex] §7Failed to save config: " + e.getMessage()));
            }

            source.sendSystemMessage(Component.literal(
                    "§b[Tebex] §7Connected to §b" + serverInformation.getServer().getName()
                            + "§7."));
            platform.configure();
            platform.refreshListings();
        }).exceptionally(ex -> {
            final Throwable cause = ex.getCause();

            if (cause instanceof ServerNotFoundException) {
                source.sendSystemMessage(Component.literal(
                        "§b[Tebex] §7Server not found. Please check your secret key."));
                platform.halt();
            } else {
                source.sendSystemMessage(
                        Component.literal("§b[Tebex] §cAn error occurred: " + cause.getMessage()));
                cause.printStackTrace();
            }

            return null;
        });
    }

    @Override
    public String getDescription() {
        return "Connects to your Tebex store.";
    }

    @Override
    public String getUsage() {
        return "<key>";
    }
}

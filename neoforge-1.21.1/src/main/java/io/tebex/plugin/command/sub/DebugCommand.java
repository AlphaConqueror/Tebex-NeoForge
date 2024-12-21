package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.sdk.platform.config.ServerPlatformConfig;
import io.tebex.sdk.util.StringUtil;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class DebugCommand extends SubCommand {

    public DebugCommand(final TebexPlugin platform) {
        super(platform, "debug", "tebex.debug");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final TebexPlugin platform = this.getPlatform();

        final ServerPlatformConfig config = platform.getPlatformConfig();
        final YamlDocument configFile = config.getYamlDocument();

        final String input = context.getArgument("trueOrFalse", String.class);
        if (StringUtil.isTruthy(input)) {
            context.getSource()
                    .sendSystemMessage(Component.literal("§b[Tebex] §7Debug mode enabled."));
            config.setVerbose(true);
            configFile.set("verbose", true);
        } else if (StringUtil.isFalsy(input)) {
            context.getSource()
                    .sendSystemMessage(Component.literal("§b[Tebex] §7Debug mode disabled."));
            config.setVerbose(false);
            configFile.set("verbose", false);
        } else {
            context.getSource().sendSystemMessage(Component.literal(
                    "§b[Tebex] §7Invalid command usage. Use /tebex " + this.getName() + " "
                            + this.getUsage()));
        }

        try {
            configFile.save();
        } catch (final IOException e) {
            context.getSource().sendSystemMessage(
                    Component.literal("§b[Tebex] §7Failed to save configuration file."));
        }
    }

    @Override
    public String getDescription() {
        return "Enables more verbose logging.";
    }

    @Override
    public String getUsage() {
        return "<trueOrFalse>";
    }
}

package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.sdk.platform.config.ServerPlatformConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ReportCommand extends SubCommand {

    public ReportCommand(final TebexPlugin platform) {
        super(platform, "report", "tebex.report");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final TebexPlugin platform = this.getPlatform();

        final ServerPlatformConfig config = platform.getPlatformConfig();
        final YamlDocument configFile = config.getYamlDocument();

        final String message = context.getArgument("message", String.class);

        if (message.isBlank()) {
            context.getSource().sendSystemMessage(
                    Component.literal("§b[Tebex] §7A message is required for your report."));
        } else {
            context.getSource().sendSystemMessage(
                    Component.literal("§b[Tebex] §7Sending your report to Tebex..."));
            platform.error("User reported error in-game: " + message);
            context.getSource()
                    .sendSystemMessage(Component.literal("§b[Tebex] §7Report sent successfully."));
        }
    }

    @Override
    public String getDescription() {
        return "Reports a problem to Tebex along with information about your webstore, server, "
                + "etc.";
    }

    @Override
    public String getUsage() {
        return "'<message>'";
    }
}

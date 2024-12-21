package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ForceCheckCommand extends SubCommand {

    private final TebexPlugin platform;

    public ForceCheckCommand(final TebexPlugin platform) {
        super(platform, "forcecheck", "tebex.forcecheck");
        this.platform = platform;
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        if (!this.platform.isSetup()) {
            context.getSource().sendSystemMessage(Component.literal("§cTebex is not setup yet!"));
            return;
        }

        // if running from console, return
        if (context.getSource().getEntity() != null) {
            context.getSource()
                    .sendSystemMessage(Component.literal("§b[Tebex] §7Performing force check..."));
        }

        this.getPlatform().performCheck(false);
    }

    @Override
    public String getDescription() {
        return "Checks immediately for new purchases.";
    }
}

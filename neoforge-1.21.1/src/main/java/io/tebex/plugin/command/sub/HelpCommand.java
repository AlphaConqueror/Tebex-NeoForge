package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.plugin.manager.CommandManager;
import java.util.Comparator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class HelpCommand extends SubCommand {

    private final CommandManager commandManager;

    public HelpCommand(final TebexPlugin platform, final CommandManager commandManager) {
        super(platform, "help", "tebex.help");
        this.commandManager = commandManager;
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();

        source.sendSystemMessage(Component.literal("§b[Tebex] §7Plugin Commands:"));

        this.commandManager.getCommands().stream().sorted(Comparator.comparing(SubCommand::getName))
                .forEach(subCommand -> source.sendSystemMessage(Component.literal(
                        " §8- §f/tebex " + subCommand.getName() + "§f" + (
                                !subCommand.getUsage().isBlank() ? " §3" + subCommand.getUsage()
                                        + " " : " ") + "§7§o(" + subCommand.getDescription()
                                + ")")));
    }

    @Override
    public String getDescription() {
        return "Shows this help page.";
    }
}

package io.tebex.plugin.command;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.gui.BuyGUI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class BuyCommand {

    private final TebexPlugin plugin;

    public BuyCommand(final TebexPlugin plugin) {
        this.plugin = plugin;
    }

    public int execute(final CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();

        try {
            final ServerPlayer player = source.getPlayer();
            new BuyGUI(this.plugin).open(player);
        } catch (final Exception e) {
            e.printStackTrace();
            source.sendSystemMessage(
                    Component.literal("§b[Tebex] §7You must be a player to run this command!"));
        }

        return 1;
    }
}

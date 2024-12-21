package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.sdk.obj.PlayerLookupInfo;
import java.util.concurrent.ExecutionException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class LookupCommand extends SubCommand {

    public LookupCommand(final TebexPlugin platform) {
        super(platform, "lookup", "tebex.lookup");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final TebexPlugin platform = this.getPlatform();

        if (!platform.isSetup()) {
            source.sendSystemMessage(Component.literal(
                    "§b[Tebex] §7This server is not connected to a webstore. Use /tebex secret to"
                            + " set your store key."));
            return;
        }

        final String username = context.getArgument("username", String.class);

        PlayerLookupInfo lookupInfo = null;
        try {
            lookupInfo = platform.getSDK().getPlayerLookupInfo(username).get();
        } catch (final InterruptedException | ExecutionException e) {
            source.sendFailure(Component.literal(
                    "§b[Tebex] §7Failed to complete player lookup. " + e.getMessage()));
            return;
        }

        source.sendSystemMessage(Component.literal(
                "§b[Tebex] §7Username: " + lookupInfo.getLookupPlayer().getUsername()));
        source.sendSystemMessage(
                Component.literal("§b[Tebex] §7Id: " + lookupInfo.getLookupPlayer().getId()));
        source.sendSystemMessage(
                Component.literal("§b[Tebex] §7Chargeback Rate: " + lookupInfo.chargebackRate));
        source.sendSystemMessage(
                Component.literal("§b[Tebex] §7Bans Total: " + lookupInfo.banCount));
        source.sendSystemMessage(
                Component.literal("§b[Tebex] §7Payments: " + lookupInfo.payments.size()));
    }

    @Override
    public String getDescription() {
        return "Gets user transaction info from your webstore.";
    }

    @Override
    public String getUsage() {
        return "<username>";
    }
}

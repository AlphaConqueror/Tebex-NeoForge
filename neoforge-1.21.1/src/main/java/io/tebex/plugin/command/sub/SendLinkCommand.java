package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.sdk.obj.CheckoutUrl;
import java.util.concurrent.ExecutionException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SendLinkCommand extends SubCommand {

    public SendLinkCommand(final TebexPlugin platform) {
        super(platform, "sendlink", "tebex.sendlink");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final TebexPlugin platform = this.getPlatform();

        final String username = context.getArgument("username", String.class).trim();
        final Integer packageId = context.getArgument("packageId", Integer.class);

        final ServerPlayer player =
                context.getSource().getServer().getPlayerList().getPlayerByName(username);
        if (player == null) {
            context.getSource().sendFailure(Component.literal(
                    "§b[Tebex] §7Could not find a player with that name on the server."));
            return;
        }

        try {
            final CheckoutUrl checkoutUrl =
                    platform.getSDK().createCheckoutUrl(packageId, username).get();
            player.sendSystemMessage(Component.literal(
                    "§b[Tebex] §7A checkout link has been created for you. Click here to complete"
                            + " payment: " + checkoutUrl.getUrl()), false);
        } catch (final InterruptedException | ExecutionException e) {
            context.getSource().sendFailure(Component.literal(
                    "§b[Tebex] §7Failed to get checkout link for package: " + e.getMessage()));
        }
    }

    @Override
    public String getDescription() {
        return "Creates payment link for a package and sends it to a player";
    }

    @Override
    public String getUsage() {
        return "<username> <packageId>";
    }
}

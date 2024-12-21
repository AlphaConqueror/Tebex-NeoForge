package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.sdk.obj.CheckoutUrl;
import java.util.concurrent.ExecutionException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class CheckoutCommand extends SubCommand {

    public CheckoutCommand(final TebexPlugin platform) {
        super(platform, "checkout", "tebex.checkout");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> context) {
        final TebexPlugin platform = this.getPlatform();

        if (!platform.isSetup()) {
            context.getSource().sendSystemMessage(Component.literal(
                    "§b[Tebex] §7This server is not connected to a webstore. Use /tebex secret to"
                            + " set your store key."));
            return;
        }

        final Integer packageId = context.getArgument("packageId", Integer.class);
        try {
            final CheckoutUrl checkoutUrl = platform.getSDK()
                    .createCheckoutUrl(packageId, context.getSource().getTextName()).get();
            context.getSource().sendSystemMessage(Component.literal(
                    "§b[Tebex] §7Checkout started! Click here to complete payment: "
                            + checkoutUrl.getUrl()));
        } catch (final InterruptedException | ExecutionException e) {
            context.getSource().sendFailure(Component.literal(
                    "§b[Tebex] §7Failed to get checkout link for package: " + e.getMessage()));
        }
    }

    @Override
    public String getDescription() {
        return "Creates payment link for a package";
    }

    @Override
    public String getUsage() {
        return "<packageId>";
    }
}

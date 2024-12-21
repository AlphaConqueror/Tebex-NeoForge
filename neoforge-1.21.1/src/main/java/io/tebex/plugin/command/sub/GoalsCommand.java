package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.sdk.obj.CommunityGoal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class GoalsCommand extends SubCommand {

    public GoalsCommand(final TebexPlugin platform) {
        super(platform, "goals", "tebex.goals");
    }

    @Override
    public void execute(final CommandContext<CommandSourceStack> sender) {
        final TebexPlugin platform = this.getPlatform();

        try {
            final List<CommunityGoal> goals = platform.getSDK().getCommunityGoals().get();
            for (final CommunityGoal goal : goals) {
                if (goal.getStatus() != CommunityGoal.Status.DISABLED) {
                    sender.getSource()
                            .sendSystemMessage(Component.literal("§b[Tebex] §7Community Goals: "));
                    sender.getSource().sendSystemMessage(Component.literal(
                            String.format("§b[Tebex] §7- %s (%.2f/%.2f) [%s]", goal.getName(),
                                    goal.getCurrent(), goal.getTarget(), goal.getStatus())));
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            sender.getSource().sendSystemMessage(
                    Component.literal("§b[Tebex] §7Unexpected response: " + e.getMessage()));
        }
    }

    @Override
    public String getDescription() {
        return "Shows active and completed community goals.";
    }
}

package io.tebex.plugin.manager;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.BuyCommand;
import io.tebex.plugin.command.SubCommand;
import io.tebex.plugin.command.sub.BanCommand;
import io.tebex.plugin.command.sub.CheckoutCommand;
import io.tebex.plugin.command.sub.DebugCommand;
import io.tebex.plugin.command.sub.ForceCheckCommand;
import io.tebex.plugin.command.sub.GoalsCommand;
import io.tebex.plugin.command.sub.HelpCommand;
import io.tebex.plugin.command.sub.InfoCommand;
import io.tebex.plugin.command.sub.LookupCommand;
import io.tebex.plugin.command.sub.ReloadCommand;
import io.tebex.plugin.command.sub.ReportCommand;
import io.tebex.plugin.command.sub.SecretCommand;
import io.tebex.plugin.command.sub.SendLinkCommand;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class CommandManager {

    private final TebexPlugin platform;
    private final List<SubCommand> commands;

    public CommandManager(final TebexPlugin platform) {
        this.platform = platform;
        this.commands = ImmutableList.of(new SecretCommand(platform), new ReloadCommand(platform),
                new ForceCheckCommand(platform), new HelpCommand(platform, this),
                new BanCommand(platform), new CheckoutCommand(platform), new DebugCommand(platform),
                new InfoCommand(platform), new LookupCommand(platform), new ReportCommand(platform),
                new SendLinkCommand(platform), new GoalsCommand(platform));
    }

    public void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        final LiteralArgumentBuilder<CommandSourceStack> baseCommand =
                literal("tebex").executes(context -> {
                    final CommandSourceStack source = context.getSource();
                    source.sendSystemMessage(Component.literal("§8[Tebex] §7Welcome to Tebex!"));
                    source.sendSystemMessage(Component.literal(
                            "§8[Tebex] §7This server is running version §fv"
                                    + this.platform.getVersion() + "§7."));

                    return 1;
                });

        if (this.platform.getPlatformConfig().isBuyCommandEnabled()) {
            final BuyCommand buyCommand = new BuyCommand(this.platform);
            dispatcher.register(
                    literal(this.platform.getPlatformConfig().getBuyCommandName()).executes(
                            buyCommand::execute));
        }

        this.commands.forEach(command -> {
            final LiteralArgumentBuilder<CommandSourceStack> subCommand =
                    literal(command.getName());

            if (command.getName().equalsIgnoreCase("secret")) {
                baseCommand.then(subCommand.then(
                        argument("key", StringArgumentType.string()).executes(context -> {
                            command.execute(context);
                            return 1;
                        })));
            } else if (command.getName().equalsIgnoreCase("debug")) {
                baseCommand.then(subCommand.then(
                        argument("trueOrFalse", StringArgumentType.string()).executes(context -> {
                            command.execute(context);
                            return 1;
                        })));
            } else if (command.getName().equalsIgnoreCase("ban")) {
                baseCommand.then(subCommand.then(
                        argument("playerName", StringArgumentType.string()).executes(context -> {
                            command.execute(context);
                            return 1;
                        })));
            } else if (command.getName().equalsIgnoreCase("checkout")) {
                baseCommand.then(subCommand.then(
                        argument("packageId", StringArgumentType.string()).executes(context -> {
                            command.execute(context);
                            return 1;
                        })));
            } else if (command.getName().equalsIgnoreCase("lookup")) {
                baseCommand.then(subCommand.then(
                        argument("username", StringArgumentType.string()).executes(context -> {
                            command.execute(context);
                            return 1;
                        })));
            } else if (command.getName().equalsIgnoreCase("report")) {
                baseCommand.then(subCommand.then(
                        argument("message", StringArgumentType.string()).executes(context -> {
                            command.execute(context);
                            return 1;
                        })));
            } else if (command.getName().equalsIgnoreCase("sendlink")) {
                baseCommand.then(subCommand.then(
                        argument("username", StringArgumentType.string()).then(
                                        argument("packageId", StringArgumentType.string()))
                                .executes(context -> {
                                    command.execute(context);
                                    return 1;
                                })));
            } else {
                baseCommand.then(subCommand.executes(context -> {
                    command.execute(context);
                    return 1;
                }));
            }
        });

        dispatcher.register(baseCommand);
    }

    public TebexPlugin getPlatform() {
        return this.platform;
    }

    public List<SubCommand> getCommands() {
        return this.commands;
    }
}

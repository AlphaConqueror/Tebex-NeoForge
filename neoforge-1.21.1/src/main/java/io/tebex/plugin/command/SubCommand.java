package io.tebex.plugin.command;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import net.minecraft.commands.CommandSourceStack;

public abstract class SubCommand {

    private final TebexPlugin platform;
    private final String name;
    private final String permission;

    public SubCommand(final TebexPlugin platform, final String name, final String permission) {
        this.platform = platform;
        this.name = name;
        this.permission = permission;
    }

    public abstract void execute(final CommandContext<CommandSourceStack> context);

    public abstract String getDescription();

    public TebexPlugin getPlatform() {
        return this.platform;
    }

    public String getName() {
        return this.name;
    }

    public String getPermission() {
        return this.permission;
    }

    public String getUsage() {
        return "";
    }
}

package io.tebex.plugin.event;

import io.tebex.plugin.TebexPlugin;
import io.tebex.sdk.obj.QueuedPlayer;
import io.tebex.sdk.obj.ServerEvent;
import io.tebex.sdk.obj.ServerEventType;
import java.util.Date;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class JoinListener {

    private final TebexPlugin plugin;

    public JoinListener(final TebexPlugin plugin) {
        this.plugin = plugin;
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    private void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        final Player player = event.getEntity();

        if (!(player instanceof final ServerPlayer serverPlayer)) {
            return;
        }

        final Object playerId =
                this.plugin.getPlayerId(serverPlayer.getName().getString(), serverPlayer.getUUID());
        this.plugin.getServerEvents()
                .add(new ServerEvent(serverPlayer.getUUID().toString(),
                        serverPlayer.getName().getString(), serverPlayer.getIpAddress(),
                        ServerEventType.JOIN, new Date().toString()));

        if (!this.plugin.getQueuedPlayers().containsKey(playerId)) {
            return;
        }

        this.plugin.handleOnlineCommands(
                new QueuedPlayer(this.plugin.getQueuedPlayers().get(playerId),
                        serverPlayer.getName().getString(), serverPlayer.getUUID().toString()));
    }
}


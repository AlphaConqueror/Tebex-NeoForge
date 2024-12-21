package io.tebex.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.ParseResults;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.event.JoinListener;
import io.tebex.plugin.manager.CommandManager;
import io.tebex.plugin.util.Multithreading;
import io.tebex.sdk.SDK;
import io.tebex.sdk.Tebex;
import io.tebex.sdk.obj.Category;
import io.tebex.sdk.obj.ServerEvent;
import io.tebex.sdk.placeholder.PlaceholderManager;
import io.tebex.sdk.platform.Platform;
import io.tebex.sdk.platform.PlatformTelemetry;
import io.tebex.sdk.platform.PlatformType;
import io.tebex.sdk.platform.config.ServerPlatformConfig;
import io.tebex.sdk.request.response.ServerInformation;
import io.tebex.sdk.util.CommandResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TebexPlugin.MOD_ID)
public class TebexPlugin implements Platform, Supplier<ModContainer> {

    // Fabric Related
    public static final String MOD_ID = "tebex";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private final String MOD_VERSION = "@VERSION@";
    private final File MOD_PATH = new File("./mods/" + MOD_ID);
    private final ModContainer container;

    private MinecraftServer server;
    private SDK sdk;
    private ServerPlatformConfig config;
    private boolean setup;
    private PlaceholderManager placeholderManager;
    private Map<Object, Integer> queuedPlayers;
    private YamlDocument configYaml;

    private ServerInformation storeInformation;
    private List<Category> storeCategories;
    private List<ServerEvent> serverEvents;

    public TebexPlugin(final IEventBus modEventBus, final ModContainer modContainer) {
        this.container = modContainer;
        modEventBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.register(this);
    }

    public void onCommonSetup(final FMLCommonSetupEvent event) {
        try {
            // Load the platform config file.
            this.configYaml = this.initPlatformConfig();
            this.config = this.loadServerPlatformConfig(this.configYaml);
        } catch (final IOException e) {
            this.warning("Failed to load configuration: " + e.getMessage(),
                    "Check that your configuration is valid and in the proper format and reload "
                            + "the plugin. You may delete `Tebex/config.yml` and a new "
                            + "configuration will be generated.");
        }
    }

    @SubscribeEvent
    public void onServerStarted(final ServerStartedEvent event) {
        this.server = event.getServer();
        this.onEnable();
    }

    @SubscribeEvent
    public void onServerStopping(final ServerStoppingEvent ignored) {
        Multithreading.shutdown();
    }

    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        new CommandManager(this).register(event.getDispatcher());
    }

    @Override
    public PlatformType getType() {
        return PlatformType.NEOFORGE;
    }

    @Override
    public String getStoreType() {
        return this.storeInformation == null ? "" : this.storeInformation.getStore().getGameType();
    }

    @Override
    public SDK getSDK() {
        return this.sdk;
    }

    @Override
    public File getDirectory() {
        return this.MOD_PATH;
    }

    @Override
    public boolean isSetup() {
        return this.setup;
    }

    @Override
    public void setSetup(final boolean setup) {
        this.setup = setup;
    }

    @Override
    public boolean isOnlineMode() {
        return this.getPlatformConfig().isProxyMode() || !this.server.isSingleplayer();
    }

    @Override
    public void configure() {
        this.setup = true;
        this.performCheck();
        this.sdk.sendTelemetry();
    }

    @Override
    public void halt() {
        this.setup = false;
    }

    @Override
    public PlaceholderManager getPlaceholderManager() {
        return this.placeholderManager;
    }

    @Override
    public Map<Object, Integer> getQueuedPlayers() {
        return this.queuedPlayers;
    }

    @Override
    public CommandResult dispatchCommand(final String command) {
        final ParseResults<CommandSourceStack> results = this.server.getCommands().getDispatcher()
                .parse(command, this.server.createCommandSourceStack());
        this.server.getCommands().performCommand(results, command);
        return CommandResult.from(
                true); // we assume success because the command manager does not report any result
    }

    @Override
    public void executeAsync(final Runnable runnable) {
        Multithreading.runAsync(runnable);
    }

    @Override
    public void executeAsyncLater(final Runnable runnable, final long time, final TimeUnit unit) {
        Multithreading.executeAsyncLater(runnable, time, unit);
    }

    @Override
    public void executeBlocking(final Runnable runnable) {
        try {
            Multithreading.executeBlocking(runnable);
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void executeBlockingLater(final Runnable runnable, final long time,
            final TimeUnit unit) {
        try {
            Multithreading.executeBlockingLater(runnable, time, unit);
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPlayerOnline(final Object player) {
        return this.getPlayer(player).isPresent();
    }

    @Override
    public int getFreeSlots(final Object playerId) {
        final ServerPlayer player = this.getPlayer(playerId).orElse(null);
        if (player == null) {
            return -1;
        }

        final NonNullList<ItemStack> inv = player.getInventory().items;
        return (int) inv.stream().filter(ItemStack::isEmpty).count();
    }

    @Override
    public String getVersion() {
        return this.MOD_VERSION;
    }

    @Override
    public void log(final Level level, final String message) {
        if (level == Level.INFO) {
            LOGGER.info(message);
        } else if (level == Level.WARNING) {
            LOGGER.warn(message);
        } else if (level == Level.SEVERE) {
            LOGGER.error(message);
        } else {
            LOGGER.info(message);
        }
    }

    @Override
    public void setStoreInfo(final ServerInformation info) {
        this.storeInformation = info;
    }

    public List<Category> getStoreCategories() {
        return this.storeCategories;
    }

    @Override
    public void setStoreCategories(final List<Category> categories) {
        this.storeCategories = categories;
    }

    @Override
    public ServerPlatformConfig getPlatformConfig() {
        return this.config;
    }

    @Override
    public PlatformTelemetry getTelemetry() {
        String serverVersion = this.server.getServerVersion();

        final Pattern pattern = Pattern.compile("MC: (\\d+\\.\\d+\\.\\d+)");
        final Matcher matcher = pattern.matcher(serverVersion);
        if (matcher.find()) {
            serverVersion = matcher.group(1);
        }

        return new PlatformTelemetry(this.getVersion(), this.server.name(), serverVersion,
                System.getProperty("java.version"), System.getProperty("os.arch"),
                !this.server.isSingleplayer());
    }

    @Override
    public String getServerIp() {
        return this.server.getLocalIp();
    }

    @Override
    public ServerInformation.Server getStoreServer() {
        return this.storeInformation.getServer();
    }

    @Override
    public ServerInformation.Store getStore() {
        return this.storeInformation.getStore();
    }

    public ServerInformation getStoreInformation() {
        return this.storeInformation;
    }

    public List<ServerEvent> getServerEvents() {
        return this.serverEvents;
    }

    @Override
    public ModContainer get() {
        return this.container;
    }

    private void onEnable() {
        // Bind SDK.
        Tebex.init(this);

        // Initialise SDK.
        this.sdk = new SDK(this, this.config.getSecretKey());
        this.placeholderManager = new PlaceholderManager();
        this.queuedPlayers = Maps.newConcurrentMap();
        this.storeCategories = new ArrayList<>();
        this.serverEvents = new ArrayList<>();

        this.placeholderManager.registerDefaults();

        // Initialise the platform.
        this.init();

        new JoinListener(this);

        this.executeAsync(() -> {
            if (!TebexPlugin.this.config.getSecretKey().isEmpty()) {
                TebexPlugin.this.info("Loading store information...");
                TebexPlugin.this.getSDK().getServerInformation()
                        .thenAccept(information -> TebexPlugin.this.storeInformation = information)
                        .exceptionally(error -> {
                            TebexPlugin.this.warning(
                                    "Failed to load server information: " + error.getMessage(),
                                    "Please check that your secret key is valid.");
                            return null;
                        });
                TebexPlugin.this.getSDK().getListing()
                        .thenAccept(listing -> TebexPlugin.this.storeCategories = listing)
                        .exceptionally(error -> {
                            TebexPlugin.this.warning(
                                    "Failed to load store categories: " + error.getMessage(),
                                    "Please check that your secret key is valid.");
                            return null;
                        });
            }
        });

        Multithreading.executeAsync(() -> {
            this.getSDK().getServerInformation()
                    .thenAccept(information -> this.storeInformation = information);
            this.getSDK().getListing().thenAccept(listing -> this.storeCategories = listing);
        }, 0, 30, TimeUnit.MINUTES);

        Multithreading.executeAsync(() -> {
            this.getSDK().sendPluginEvents();
        }, 0, 10, TimeUnit.MINUTES);

        Multithreading.executeAsync(() -> {
            final List<ServerEvent> runEvents = Lists.newArrayList(
                    this.serverEvents.subList(0, Math.min(this.serverEvents.size(), 750)));
            if (runEvents.isEmpty()) {
                return;
            }

            this.sdk.sendJoinEvents(runEvents).thenAccept(aVoid -> {
                this.serverEvents.removeAll(runEvents);
                this.debug("Successfully sent join events");
            }).exceptionally(throwable -> {
                this.error("Failed to send join events: " + throwable.getMessage(), throwable);
                return null;
            });
        }, 0, 1, TimeUnit.MINUTES);
    }

    private Optional<ServerPlayer> getPlayer(final Object player) {
        if (player == null) {
            return Optional.empty();
        }

        if (this.isOnlineMode() && !this.isGeyser() && player instanceof UUID) {
            return Optional.ofNullable(this.server.getPlayerList().getPlayer((UUID) player));
        }

        return Optional.ofNullable(this.server.getPlayerList().getPlayerByName((String) player));
    }
}

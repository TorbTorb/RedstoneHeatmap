package redstone_heatmap.client;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Redstone_heatmapClient implements ClientModInitializer {
    private static int tickCounter = 0;
    public static final String MOD_ID = "redstone_heatmap";
    public static final String MOD_STRING = "§eHeatmap: §r";
    public static final Logger LOGGER = LoggerFactory.getLogger(Redstone_heatmapClient.MOD_ID);

    //TODO:
    // stats:
    //      Total
    //      per block (clickable?)
    // track for certain amount of ticks
    // better coloring (median, 90th percentile smth)
    // faster rendering (how?) (chunk based? no clue)
    //
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> HeatMapCommand.register(dispatcher));
        WorldRenderEvents.AFTER_TRANSLUCENT.register(HeatmapRender::render);
        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);

    }

    private void onClientTick(MinecraftClient client) {
        tickCounter += 1;
        HeatMapCommand.tick(client);
    }

    public static int getTickCounter() {
        return tickCounter;
    }
}


package redstone_heatmap.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class HeatMapCommand {
    private static BlockPos Pos1;
    private static BlockPos Pos2;
    private static Boolean Running = Boolean.FALSE;
    private static Boolean Paused = Boolean.FALSE;
    private static Boolean ShowPreview = Boolean.TRUE;
    private final static ArrayList<HeatmapBlock> HeatmapBlocks = new ArrayList<>();
    private static float AverageUpdateCount = 0;
    private static int Median = 0;
    private static int Percentile90th = 0;
    private static int Percentile10th = 0;
    private static int TotalUpdates = 0;
    private static int MaxUpdateCount = 0;
    private static int Duration = 0;
    private static int RunForTicks = -1;

    public static void updateStats() {
        ArrayList<Integer> updateList = new ArrayList<>(HeatmapBlocks.size());
        TotalUpdates = 0;
        MaxUpdateCount = 0;
        AverageUpdateCount = 0;

        for (int i = 0; i < HeatmapBlocks.size(); i++) {
            int updateCount = HeatmapBlocks.get(i).getUpdateCount();
            MaxUpdateCount = Math.max(MaxUpdateCount, updateCount);
            AverageUpdateCount += updateCount;
            TotalUpdates += updateCount;
            updateList.add(i, updateCount);
        }
        AverageUpdateCount /= HeatmapBlocks.size();

        Collections.sort(updateList);
        int n = updateList.size();
        Median = updateList.get((int)(n*0.5f));
        Percentile90th = updateList.get((int)(n*0.9f));
        Percentile10th = updateList.get((int)(n*0.1f));
    }
    public static void tick(final MinecraftClient client) {

        if(client.world == null || !HeatMapCommand.Running || HeatMapCommand.Paused || Redstone_heatmapClient.getTickCounter()%2!=0)
            return;

        if(RunForTicks-- == 0) {
            client.inGameHud.getChatHud().addMessage(Text.literal(Redstone_heatmapClient.MOD_STRING + "§aDone: Paused recording"));
            Paused = Boolean.TRUE;
            return;
        }
        Duration += 1;

        updateStats();

        for(HeatmapBlock block : HeatmapBlocks) {
            block.Update(client.world.getBlockState(block.getPos()));
        }
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("heatmap")

                .then(ClientCommandManager.literal("pos1")
                        //.then(ClientCommandManager.argument("Pos1", BlockPosArgumentType.blockPos())
                        //    .executes(HeatMapCommand::executePos1Provided)
                        //)
                        .executes(HeatMapCommand::executePos1Player)
                )
                .then(ClientCommandManager.literal("pos2")
                        //.then(ClientCommandManager.argument("Pos2", BlockPosArgumentType.blockPos())
                        //        .executes(HeatMapCommand::executePos2Provided)
                        //)
                        .executes(HeatMapCommand::executePos2Player)
                )
                .then(ClientCommandManager.literal("stop")
                        .executes((HeatMapCommand::executeStop))
                )
                .then(ClientCommandManager.literal("start")
                        .executes(context -> executeStart(context, -1))
                            .then(ClientCommandManager.argument("Tick Count", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                .executes(context -> executeStart(context, IntegerArgumentType.getInteger(context, "Tick Count"))))
                )
                .then(ClientCommandManager.literal("pause")
                        .executes((HeatMapCommand::executePause))
                )
                .then(ClientCommandManager.literal("resume")
                        .executes(context -> executeResume(context, -1))
                            .then(ClientCommandManager.argument("Tick Count", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                .executes(context -> executeResume(context, IntegerArgumentType.getInteger(context, "Tick Count"))))
                )
                .then(ClientCommandManager.literal("render")
                    .executes(context -> executeRender(context, 1024))
                        .then(ClientCommandManager.argument("Image Size", IntegerArgumentType.integer(1, 20000))
                                .executes(context -> executeRender(context, IntegerArgumentType.getInteger(context, "Image Size"))))

                )
                .then(ClientCommandManager.literal("show")
                        .executes((HeatMapCommand::executeShow))
                )
                .then(ClientCommandManager.literal("hide")
                        .executes((HeatMapCommand::executeHide))
                )
                .then(ClientCommandManager.literal("stats")
                        .executes((HeatMapCommand::executeStats))
                )

        );
    }
    /*private static int executePos1Provided(CommandContext<FabricClientCommandSource> source) {
        BlockPos pos = BlockPosArgumentType.getBlockPos(source, "Pos1");
        ClientBlockPosArgumentType
        return Command.SINGLE_SUCCESS;
    }*/

    private static int executePos1Player(final CommandContext<FabricClientCommandSource> source) {
        if(source.getSource().getWorld() == null || source.getSource().getClient().player == null) return -1;
        if (Running){
            source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "§cCannot change corner points while running"));
            return -1;
        }
        Pos1 = source.getSource().getClient().player.getBlockPos();
        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "Set position 1 to: §a" + Pos1.toShortString()));

        return Command.SINGLE_SUCCESS;
    }
    private static int executePos2Player(final CommandContext<FabricClientCommandSource> source) {
        if(source.getSource().getWorld() == null || source.getSource().getClient().player == null) return -1;
        if (Running) {
            source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "§cCannot change corner points while running"));
            return -1;
        }
        Pos2 = source.getSource().getClient().player.getBlockPos();
        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "Set position 2 to: §a" + Pos2.toShortString()));
        return Command.SINGLE_SUCCESS;
    }
    private static int executeStop(final CommandContext<FabricClientCommandSource> source) {
        if (!Running) {
            source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "§cCannot stop while not running"));
            return -1;
        }
        Running = Boolean.FALSE;
        Paused = Boolean.FALSE;

        HeatmapBlocks.clear();

        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "Stopped recording"));
        return Command.SINGLE_SUCCESS;
    }
    private static int executeStart(final CommandContext<FabricClientCommandSource> source, final int tickCount) {
        if (Running){
            executeStop(source);
        }
        if (Pos1 == null || Pos2 == null) {
            source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "§cNot all positions have been set"));
            return -1;
        }
        //reset tick counter
        Duration = 0;
        RunForTicks = tickCount; //-1 if none provided
        //initialize hashmaps
        HeatmapBlocks.clear();
        //make it so only redstone components get added?
        //probs shouldn't ever happen but better safe than sorry
        if (source.getSource().getWorld() == null || source.getSource().getClient() == null) return -1;

        int posX1 = Math.min(Pos1.getX(), Pos2.getX());
        int posY1 = Math.min(Pos1.getY(), Pos2.getY());
        int posZ1 = Math.min(Pos1.getZ(), Pos2.getZ());

        int posX2 = Math.max(Pos1.getX(), Pos2.getX());
        int posY2 = Math.max(Pos1.getY(), Pos2.getY());
        int posZ2 = Math.max(Pos1.getZ(), Pos2.getZ());

        for (int x = posX1; x <= posX2; x++) {
            for (int y = posY1; y <= posY2; y++) {
                for (int z = posZ1; z <= posZ2; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = source.getSource().getWorld().getBlockState(pos).getBlock();
                    if (!(block instanceof RepeaterBlock
                            || block instanceof RedstoneWireBlock
                            || block instanceof ComparatorBlock
                            || block instanceof RedstoneTorchBlock
                            || block instanceof WallRedstoneTorchBlock
                            || block instanceof PistonBlock
                            || block instanceof ObserverBlock
                            || block instanceof RedstoneLampBlock
                            || block instanceof DispenserBlock
                            || block instanceof DropperBlock
                            || block instanceof HopperBlock
                            || block instanceof CauldronBlock
                            || block instanceof TrapdoorBlock
                            || block instanceof DoorBlock
                            || block instanceof SculkSensorBlock
                            || block instanceof LightningRodBlock
                            || block instanceof DaylightDetectorBlock
                            || block instanceof PoweredRailBlock
                            || block instanceof DetectorRailBlock
                            || block instanceof NoteBlock
                            || block instanceof TntBlock
                            || block instanceof JukeboxBlock
                            || block instanceof TripwireHookBlock
                            || block instanceof TripwireBlock
                            || block instanceof TargetBlock
                            || block instanceof BeaconBlock
                            || block instanceof BigDripleafBlock
                            || block instanceof FenceGateBlock
                            || block instanceof WallBlock
                            || block instanceof FenceBlock
                            || block instanceof EndGatewayBlock)) continue;
                    HeatmapBlocks.add(new HeatmapBlock(pos, source.getSource().getWorld().getBlockState(pos)));
                }
            }
        }
        Running = Boolean.TRUE;
        Paused = Boolean.FALSE;
        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "Started recording "  + (tickCount==-1?"":("for " + tickCount + "ticks "))));
        return Command.SINGLE_SUCCESS;
    }
    private static int executePause(final CommandContext<FabricClientCommandSource> source) {
        if (!Running) {
            source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "§cCannot pause while not running"));
            return -1;
        }
        Paused = Boolean.TRUE;
        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "Paused recording"));
        return Command.SINGLE_SUCCESS;
    }
    private static int executeResume(final CommandContext<FabricClientCommandSource> source, final int tickCount) {
        if (!Running) {
            source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "§cCannot resume while not running"));
            return -1;
        }
        RunForTicks = tickCount;
        Paused = Boolean.FALSE;

        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "Resumed Heatmap " + (tickCount==-1?"":("for " + tickCount + "ticks ")) + "§a/heatmap pause'§r to pause"));
        return Command.SINGLE_SUCCESS;
    }
    private static int executeRender(final CommandContext<FabricClientCommandSource> source, final int imageSize) {
        if (Pos1 == null || Pos2 == null) {
            source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "§cNot all positions have been set"));
            return -1;
        }
        if (HeatmapBlocks.isEmpty()) {
            source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "§cNo recorded data to render"));
            return -1;
        }

        ImageGenerator.createImage(HeatmapBlocks, Pos1, Pos2, imageSize);
        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "Saved Image to root (minecraft folder)"));
        return Command.SINGLE_SUCCESS;
    }
    private static int executeShow(final CommandContext<FabricClientCommandSource> source) {
        ShowPreview = Boolean.TRUE;
        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "Enabled in-game rendering"));
        return Command.SINGLE_SUCCESS;
    }
    private static int executeHide(final CommandContext<FabricClientCommandSource> source) {
        ShowPreview = Boolean.FALSE;
        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "Disabled in-game rendering"));
        return Command.SINGLE_SUCCESS;
    }
    private static int executeStats(final CommandContext<FabricClientCommandSource> source) {
        if(HeatmapBlocks.isEmpty()){
            source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "§cNo recorded Data to calculate stats from"));
            return -1;
        }
        //recompute state
        updateStats();
        source.getSource().sendFeedback(Text.literal(Redstone_heatmapClient.MOD_STRING + "\n    Duration in ticks: §a" + Duration
                + "\n    §fTotal Updates: §a" + TotalUpdates
                + "\n    §fAverage Updates per Tick: §a" + (float)TotalUpdates / Duration
                + "\n    §fAverage Updates per Component: §a" + AverageUpdateCount
                + "\n    §fAverage Updates per Tick per Component: §a" + AverageUpdateCount / Duration
        ));
        return Command.SINGLE_SUCCESS;

    }
    public static Boolean getShowPreview() {
        return ShowPreview;
    }

    public static ArrayList<HeatmapBlock> getHeatmapBlocks() {
        return HeatmapBlocks;
    }

    public static int getMaxUpdateCount() {
        return MaxUpdateCount;
    }

    public static int getMedian() {
        return Median;
    }

    public static float getAverageUpdateCount() {
        return AverageUpdateCount;
    }

    public static int getPercentile90th() {
        return Percentile90th;
    }

    public static int getPercentile10th() {
        return Percentile10th;
    }
}

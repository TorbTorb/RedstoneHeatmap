package redstone_heatmap.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.awt.*;


public class HeatmapBlock {
    private BlockState PreviousState;
    private int UpdateCount;
    private int color;
    private final BlockPos pos;
    //public VoxelShape cachedShape;



    HeatmapBlock(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.PreviousState = state;
        this.UpdateCount = 0;
        this.color = Color.BLUE.getRGB();    //get the darkest color
        //this.cachedShape = world.getBlockState(Pos).getOutlineShape(world, Pos);
    }
    //call this every tick
    //checks for changes
    public void Update(final BlockState currentState){

        if (!currentState.equals(this.PreviousState)) {
            this.PreviousState = currentState;
            this.UpdateCount += 1;
        }
        this.color = ColorMap.BlueRedYellowMap(this.UpdateCount);
        }

    public BlockState getPreviousState() {
        return this.PreviousState;
    }
    public int getUpdateCount() {
        return this.UpdateCount;
    }
    public int getColor() {
        return this.color;
    }
    public BlockPos getPos() {
        return this.pos;
    }
}

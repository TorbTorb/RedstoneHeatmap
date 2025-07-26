package redstone_heatmap.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;


public class HeatmapRender {
    final private static float offset = 0.001f;

    public static void render(final WorldRenderContext wrc)
    {
        if (HeatMapCommand.getShowPreview()) {
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder bufferBuilder = tessellator.getBuffer();
            final ClientWorld world = wrc.world();
            final Camera camera = wrc.camera();
            final Vec3d cameraPos = camera.getPos();

            MatrixStack matrices = wrc.matrixStack();
            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            final Matrix4f model = matrices.peek().getPositionMatrix();

            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            HeatMapCommand.getHeatmapBlocks().forEach((HeatmapBlock block)
                    -> drawFilledBox(block.getPos(), bufferBuilder, block.getPreviousState().getOutlineShape(world, block.getPos()), block.getColor(), model));


            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            tessellator.draw();

            matrices.pop();
        }
    }

    private static void drawFilledBox(final BlockPos pos, final BufferBuilder buf, final VoxelShape voxel, final int color, Matrix4f model) {
        final float ypos = pos.getY();// - cameraY;
        final float zpos = pos.getZ();// - cameraZ;
        final float xpos = pos.getX();// - cameraX;

        float x, y, z, topx, topy, topz;

        if (voxel.isEmpty()) {
            x = xpos;
            y = ypos;
            z = zpos;
            topx = x+1;
            topy = y+1;
            topz = z+1;
        }
        else  {
            x = xpos - offset + (float)voxel.getMin(Direction.Axis.X);
            y = ypos - offset + (float)voxel.getMin(Direction.Axis.Y);
            z = zpos - offset + (float)voxel.getMin(Direction.Axis.Z);

            topx = xpos + offset + (float)voxel.getMax(Direction.Axis.X);
            topy = ypos + offset + (float)voxel.getMax(Direction.Axis.Y);
            topz = zpos + offset + (float)voxel.getMax(Direction.Axis.Z);
        }

        buf.vertex(model, x, 		y, 		z).		color(color).next();
        buf.vertex(model, topx, 	y, 		z).		color(color).next();
        buf.vertex(model, topx, 	y, 		topz).	color(color).next();
        buf.vertex(model, x, 		y, 		topz).	color(color).next();

        buf.vertex(model, x, 		topy,	z).		color(color).next();
        buf.vertex(model, x, 		topy,	topz).	color(color).next();
        buf.vertex(model, topx, 	topy,	topz).	color(color).next();
        buf.vertex(model, topx, 	topy,	z).		color(color).next();

        buf.vertex(model, x, 		y, 		z).		color(color).next();
        buf.vertex(model, x, 		topy,	z).		color(color).next();
        buf.vertex(model, topx, 	topy,	z).		color(color).next();
        buf.vertex(model, topx,	    y,		z).		color(color).next();

        buf.vertex(model, x, 		y, 		topz).	color(color).next();
        buf.vertex(model, topx, 	y, 		topz).	color(color).next();
        buf.vertex(model, topx, 	topy,	topz).	color(color).next();
        buf.vertex(model, x, 		topy,	topz).	color(color).next();

        buf.vertex(model, topx, 	y, 		z).		color(color).next();
        buf.vertex(model, topx, 	topy, 	z).		color(color).next();
        buf.vertex(model, topx, 	topy,	topz).	color(color).next();
        buf.vertex(model, topx, 	y,		topz).	color(color).next();

        buf.vertex(model, x, 		y, 		z).		color(color).next();
        buf.vertex(model, x, 		y,		topz).	color(color).next();
        buf.vertex(model, x, 		topy,	topz).	color(color).next();
        buf.vertex(model, x, 		topy, 	z).		color(color).next();
    }
}
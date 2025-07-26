package redstone_heatmap.client;

import net.minecraft.util.math.BlockPos;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageGenerator {
    public static void createImage(final ArrayList<HeatmapBlock> heatmapBlocks, final BlockPos pos1, final BlockPos pos2, final int imageSize) {
        final int[] maxUpdateCount = {0};
        final float[] averageUpdateCount = {0.f};

        final int posY1 = Math.min(pos1.getY(), pos2.getY());
        final int posX1 = Math.min(pos1.getX(), pos2.getX());
        final int posZ1 = Math.min(pos1.getZ(), pos2.getZ());

        final int posX2 = Math.max(pos1.getX(), pos2.getX());
        final int posY2 = Math.max(pos1.getY(), pos2.getY());
        final int posZ2 = Math.max(pos1.getZ(), pos2.getZ());

        final int widthBlocks = posX2 - posX1 + 1;
        final int heightBlocks = posZ2 - posZ1 + 1;

        final int width, height;

        if (widthBlocks > heightBlocks) {
            width = imageSize;
            height = (int)(imageSize * ((float)heightBlocks/widthBlocks));
        }
        else {
            width = (int)(imageSize * ((float)widthBlocks/heightBlocks));
            height = imageSize;
        }

        final float widthPerBlock = (float)width/(widthBlocks);
        final float heightPerBlock = (float)height/(heightBlocks);

        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        HeatMapCommand.updateStats();
        //buffer all blocks into a hashmap for faster access
        final HashMap<BlockPos, HeatmapBlock> blocksMap = new HashMap<>(heatmapBlocks.size());
        for(HeatmapBlock block: heatmapBlocks) {
            blocksMap.put(block.getPos(), block);
        }


        for (int x = posX1; x <= posX2; x++) {
            for (int z = posZ1; z <= posZ2; z++) {
                int updatesInColumn = 0;
                int heatmapBlocksInColumn = 0;
                int color = 0;

                for (int y = posY1; y <= posY2; y++) {
                    HeatmapBlock heatmapBlock = blocksMap.get(new BlockPos(x, y, z));
                    if (heatmapBlock == null)
                        continue;
                    updatesInColumn += heatmapBlock.getUpdateCount();
                    heatmapBlocksInColumn += 1;
                }
                //avoid div by 0
                if (heatmapBlocksInColumn != 0) {
                    updatesInColumn = updatesInColumn / heatmapBlocksInColumn;
                    color = ColorMap.BlueRedYellowMap(updatesInColumn);
                }

                for (int xImg = (int)((-posX1 + x) * widthPerBlock); xImg < (int)((-posX1 + x + 1) * widthPerBlock); xImg++) {
                    for (int yImg = (int)((-posZ1 + z) * heightPerBlock); yImg < (int)((-posZ1 + z + 1) * heightPerBlock); yImg++) {
                        img.setRGB(Math.min(width-1, xImg), Math.min(height-1, yImg), color);
                    }
                }
            }
        }

        saveImageToFile(img, "heatmap.png");
    }

    private static void saveImageToFile(final BufferedImage img, final String filePath) {
        try {
            File outputfile = new File(filePath);
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            Redstone_heatmapClient.LOGGER.error(e.getMessage(), e);
        }
    }
}

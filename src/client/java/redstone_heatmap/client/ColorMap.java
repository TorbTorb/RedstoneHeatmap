package redstone_heatmap.client;

import java.awt.*;

public class ColorMap {
    private static final Color BRIGHT_YELLOW = new Color(255, 255, 85);

    public static Color interpolate(final Color color1, final Color color2, float t) {
        t = Math.max(0, Math.min(1, t));

        int red = (int) (color1.getRed() * (1 - t) + color2.getRed() * t);
        int green = (int) (color1.getGreen() * (1 - t) + color2.getGreen() * t);
        int blue = (int) (color1.getBlue() * (1 - t) + color2.getBlue() * t);

        return new Color(red, green, blue);
    }

    public static int BlueRedYellowMap(final int updateCount) {
        Color res;
        float val;
        if (updateCount <= HeatMapCommand.getMedian()) {     //yellow red inter
            val = (updateCount - HeatMapCommand.getPercentile10th()) / Math.max(1.f,  HeatMapCommand.getMedian());
            val = utils.clamp(val, 0.f, 1.f);
            res = interpolate(Color.BLUE, Color.RED, val);
        }
        else {
            val = Math.min(1.f, (updateCount - HeatMapCommand.getMedian()) / Math.max(1.f, ((HeatMapCommand.getPercentile90th())-HeatMapCommand.getMedian())));
            val = utils.clamp(val, 0.f, 1.f);
            res = interpolate(Color.RED, BRIGHT_YELLOW, val);
        }
        return res.getRGB();
    }
}

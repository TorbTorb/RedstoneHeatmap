package redstone_heatmap.client;

public class utils {
    public static float clamp(float val, float min, float max) {
        return Math.min(max, Math.max(min, val));
    }
}

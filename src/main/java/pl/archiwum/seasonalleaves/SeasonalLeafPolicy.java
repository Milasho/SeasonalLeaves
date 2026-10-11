package pl.archiwum.seasonalleaves;

import net.minecraft.core.BlockPos;

/** Deterministic per-position leaf shedding; does not mutate world blocks. */
public final class SeasonalLeafPolicy {
    private SeasonalLeafPolicy() {}

    /**
     * Returns true if the leaf at this position should be hidden.
     * shedFraction is clamped to [0, 1], so the same block always makes the same
     * choice at a given shedding stage and chunk rebuilds do not flicker randomly.
     */
    public static boolean shouldHide(BlockPos pos, float shedFraction) {
        float fraction = Math.max(0.0f, Math.min(1.0f, shedFraction));
        if (fraction <= 0.0f) return false;
        if (fraction >= 1.0f) return true;
        int hash = pos.getX() * 73428767 ^ pos.getY() * 912931 ^ pos.getZ() * 42317861;
        hash ^= (hash >>> 16);
        hash *= 0x7feb352d;
        hash ^= (hash >>> 15);
        hash *= 0x846ca68b;
        hash ^= (hash >>> 16);
        float value = (hash & 0x7fffffff) / (float) Integer.MAX_VALUE;
        return value < fraction;
    }
}

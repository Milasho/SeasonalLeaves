package pl.archiwum.seasonalleaves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class LeafSeasonClientTicker {
    private static final int SHEDDING_STEPS = 16;
    private static ClientLevel observedLevel;
    private static int lastStep = -1;
    private static String lastSubSeason = "";

    private LeafSeasonClientTicker() {}

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        float shedFraction = SeasonReader.update(level);
        if (level == null) {
            observedLevel = null;
            lastStep = -1;
            lastSubSeason = "";
            return;
        }

        int step = Math.round(shedFraction * SHEDDING_STEPS);
        String subSeason = SeasonReader.subSeason();
        if (level != observedLevel) {
            observedLevel = level;
            lastStep = step;
            lastSubSeason = subSeason;
            PoplarSaturationTextures.update(SeasonReader.poplarSaturationMultiplier());
            return;
        }
        if (step == lastStep && subSeason.equals(lastSubSeason)) return;

        boolean subSeasonChanged = !subSeason.equals(lastSubSeason);
        lastStep = step;
        lastSubSeason = subSeason;
        if (subSeasonChanged) {
            PoplarSaturationTextures.update(SeasonReader.poplarSaturationMultiplier());
        }
        minecraft.levelRenderer.invalidateCompiledGeometry(
                level, minecraft.options, minecraft.gameRenderer.mainCamera(), minecraft.getBlockColors());
    }
}
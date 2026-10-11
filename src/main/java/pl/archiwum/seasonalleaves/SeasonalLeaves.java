package pl.archiwum.seasonalleaves;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SeasonalLeaves.MOD_ID)
public final class SeasonalLeaves {
    public static final String MOD_ID = "seasonalleaves";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public SeasonalLeaves(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
        LOGGER.info("Seasonal Leaves loaded. Leaf rendering is controlled client-side by the mixin.");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> NeoForge.EVENT_BUS.addListener(LeafSeasonClientTicker::onClientTick));
    }
}

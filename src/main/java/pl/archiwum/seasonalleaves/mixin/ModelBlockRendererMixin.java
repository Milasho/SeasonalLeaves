package pl.archiwum.seasonalleaves.mixin;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.archiwum.seasonalleaves.SeasonReader;
import pl.archiwum.seasonalleaves.SeasonalLeafPolicy;

import java.util.Locale;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true, remap = true)
    private void seasonalleaves$hideLeaves(BlockQuadOutput output, float x, float y, float z,
                                           BlockAndTintGetter level, BlockPos pos, BlockState state,
                                           BlockStateModel model, long seed, CallbackInfo ci) {
        if (!isLeafBlock(state)) return;
        float shed = SeasonReader.shedFraction();
        if (SeasonalLeafPolicy.shouldHide(pos, shed)) ci.cancel();
    }

    private static boolean isLeafBlock(BlockState state) {
        if (state.is(BlockTags.LEAVES) || state.getBlock() instanceof LeavesBlock) return true;
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().toLowerCase(Locale.ROOT);
        return blockId.contains("leaf");
    }
}
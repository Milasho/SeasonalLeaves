package pl.archiwum.seasonalleaves.mixin;

import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {
    @Invoker("uploadInitialContents")
    void seasonalleaves$uploadInitialContents();
}
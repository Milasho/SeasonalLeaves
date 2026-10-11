package pl.archiwum.seasonalleaves;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import pl.archiwum.seasonalleaves.mixin.TextureAtlasAccessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PoplarSaturationTextures {
    private static final Map<Identifier, NativeImage> ORIGINAL_TEXTURES = new ConcurrentHashMap<>();

    private PoplarSaturationTextures() {}

    public static void capture(Identifier name, NativeImage image) {
        if (!isPoplarTexture(name)) return;
        NativeImage original = image.mappedCopy(pixel -> pixel);
        NativeImage previous = ORIGINAL_TEXTURES.put(name, original);
        if (previous != null) previous.close();
    }

    public static void update(float saturationMultiplier) {
        Minecraft minecraft = Minecraft.getInstance();
        TextureAtlas atlas = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        boolean changed = false;

        for (TextureAtlasSprite sprite : atlas.getTextures().values()) {
            SpriteContents contents = sprite.contents();
            NativeImage original = ORIGINAL_TEXTURES.get(contents.name());
            if (original == null) continue;

            NativeImage target = contents.getOriginalImage();
            int mipLevel = contents.byMipLevel.length - 1;
            for (int level = 1; level < contents.byMipLevel.length; level++) {
                contents.byMipLevel[level].close();
            }

            for (int y = 0; y < original.getHeight(); y++) {
                for (int x = 0; x < original.getWidth(); x++) {
                    target.setPixel(x, y, adjustSaturation(original.getPixel(x, y), saturationMultiplier));
                }
            }

            contents.byMipLevel = new NativeImage[]{target};
            contents.increaseMipLevel(mipLevel);
            changed = true;
        }

        if (changed) ((TextureAtlasAccessor) atlas).seasonalleaves$uploadInitialContents();
    }

    private static boolean isPoplarTexture(Identifier name) {
        return name.getNamespace().equals("minecraft")
                && name.getPath().matches("block/(red|orange|yellow)_poplar_leaves");
    }

    private static int adjustSaturation(int color, float multiplier) {
        int alpha = color >>> 24;
        if (alpha == 0) return color;

        float red = (color >>> 16 & 0xFF) / 255.0f;
        float green = (color >>> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;
        if (delta == 0.0f || max == 0.0f) return color;

        float hue;
        if (max == red) {
            hue = 60.0f * (((green - blue) / delta) % 6.0f);
        } else if (max == green) {
            hue = 60.0f * ((blue - red) / delta + 2.0f);
        } else {
            hue = 60.0f * ((red - green) / delta + 4.0f);
        }
        if (hue < 0.0f) hue += 360.0f;

        float chroma = delta * multiplier;
        float secondary = chroma * (1.0f - Math.abs((hue / 60.0f) % 2.0f - 1.0f));
        float offset = max - chroma;
        float adjustedRed;
        float adjustedGreen;
        float adjustedBlue;

        if (hue < 60.0f) {
            adjustedRed = chroma;
            adjustedGreen = secondary;
            adjustedBlue = 0.0f;
        } else if (hue < 120.0f) {
            adjustedRed = secondary;
            adjustedGreen = chroma;
            adjustedBlue = 0.0f;
        } else if (hue < 180.0f) {
            adjustedRed = 0.0f;
            adjustedGreen = chroma;
            adjustedBlue = secondary;
        } else if (hue < 240.0f) {
            adjustedRed = 0.0f;
            adjustedGreen = secondary;
            adjustedBlue = chroma;
        } else if (hue < 300.0f) {
            adjustedRed = secondary;
            adjustedGreen = 0.0f;
            adjustedBlue = chroma;
        } else {
            adjustedRed = chroma;
            adjustedGreen = 0.0f;
            adjustedBlue = secondary;
        }

        int resultRed = Math.round((adjustedRed + offset) * 255.0f);
        int resultGreen = Math.round((adjustedGreen + offset) * 255.0f);
        int resultBlue = Math.round((adjustedBlue + offset) * 255.0f);
        return alpha << 24 | resultRed << 16 | resultGreen << 8 | resultBlue;
    }
}
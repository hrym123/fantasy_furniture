package org.lanye.fantasy_furniture.content.furniture.decor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 普通玻璃窗：与 Blockbench / bbmodel 中「同一套」多槽贴图对应；五套造型 geo 共用本组 PNG（不按造型复制）。
 *
 * <p>命名：<code>textures/block/plain_glass_window_&lt;槽索引&gt;_&lt;颜色后缀&gt;.png</code>（方块 id + 槽位 + 由图像主色推断的英文颜色名，与
 * {@code tools/plain_glass_window_texture_naming.py} 一致）。重新导出后若主色变化导致文件名变化，须同步修改 {@link #TEXTURE_STEMS}。
 *
 * <p>GeckoLib 的 {@link software.bernie.geckolib.model.GeoModel#getTextureResource} 仅返回一张主纹理，
 * 当前以 {@link #PRIMARY_SLOT} 对应文件为准。
 */
public final class PlainGlassWindowSharedTextures {

    /** 与 bbmodel {@code textures} 数组长度一致。 */
    public static final int SLOT_COUNT = 9;

    /** 主纹理槽（通常为 textures[0]）。 */
    public static final int PRIMARY_SLOT = 0;

    /**
     * 与 {@code textures/block/plain_glass_window_<槽>_<色>.png} 的 stem 一致；索引即 bbmodel {@code textures[]} 顺序；
     * 色名由导出脚本按像素主色推断，重新导出后若后缀变化须同步修改本数组。
     */
    public static final String[] TEXTURE_STEMS = {
        "plain_glass_window_0_white",
        "plain_glass_window_1_black",
        "plain_glass_window_2_tan",
        "plain_glass_window_3_ice_blue",
        "plain_glass_window_4_cream",
        "plain_glass_window_5_pale_green",
        "plain_glass_window_6_cream",
        "plain_glass_window_7_mixed",
        "plain_glass_window_8_cream",
    };

    private PlainGlassWindowSharedTextures() {}

    public static String textureStem(int slot) {
        return TEXTURE_STEMS[Mth.clamp(slot, 0, SLOT_COUNT - 1)];
    }

    public static String texturePath(int slot) {
        return "textures/block/" + textureStem(slot);
    }

    public static ResourceLocation textureLocation(String modid, int slot) {
        return ResourceLocation.fromNamespaceAndPath(modid, texturePath(slot) + ".png");
    }

    /** 与 {@link #TEXTURE_STEMS} 中任意 stem 对应的主世界路径（含 {@code textures/block/} 与 {@code .png}）。 */
    public static ResourceLocation textureLocationForStem(String modid, String stem) {
        return ResourceLocation.fromNamespaceAndPath(modid, "textures/block/" + stem + ".png");
    }
}

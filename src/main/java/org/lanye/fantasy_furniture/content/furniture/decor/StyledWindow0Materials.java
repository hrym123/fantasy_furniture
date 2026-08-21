package org.lanye.fantasy_furniture.content.furniture.decor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lanye.fantasy_furniture.content.furniture.common.state.StyledWindow0MaterialVariant;

/**
 * 0号窗户「材质」：与 {@link StyledWindow0SharedTextures#SLOT_COUNT}（当前 9）一一对应；每种颜色一对
 * {@code styled_window_0_<颜色后缀>} block/item 同 id（REG-608；与型号窗 {@code styled_window_1} 等同前缀）。
 *
 * <p>每套 {@link #stem(int, int)} 当前均为 {@link StyledWindow0SharedTextures#TEXTURE_STEMS} 的拷贝；世界中与手持预览均用
 * {@link #itemPreviewStem(int)}（第 {@code material} 槽对应 PNG）。贴图文件 stem 仍为历史
 * {@code styled_window_0_*}。
 */
public final class StyledWindow0Materials {

    private static final List<String[]> STEM_SETS = buildStemSets();

    private static List<String[]> buildStemSets() {
        String[] src = StyledWindow0SharedTextures.TEXTURE_STEMS;
        int n = StyledWindow0SharedTextures.SLOT_COUNT;
        List<String[]> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(Arrays.copyOf(src, src.length));
        }
        return List.copyOf(list);
    }

    static {
        if (StyledWindow0MaterialVariant.values().length != STEM_SETS.size()) {
            throw new IllegalStateException(
                    "StyledWindow0Materials: StyledWindow0MaterialVariant count vs STEM_SETS mismatch");
        }
        for (int i = 0; i < STEM_SETS.size(); i++) {
            if (STEM_SETS.get(i).length != StyledWindow0SharedTextures.SLOT_COUNT) {
                throw new IllegalStateException("StyledWindow0Materials: stem set " + i + " length mismatch");
            }
        }
    }

    private StyledWindow0Materials() {}

    public static int count() {
        return STEM_SETS.size();
    }

    /** 物品注册名与 {@link StyledWindow0MaterialVariant} 序列化名一致。 */
    public static String itemSuffix(int material) {
        return StyledWindow0MaterialVariant.byIndex(material).getSerializedName();
    }

    public static String stem(int material, int slot) {
        return STEM_SETS.get(material)[slot];
    }

    /** 世界中与 {@link StyledWindow0SharedTextures#PRIMARY_SLOT} 对齐的主纹理 stem。 */
    public static String primaryStem(int material) {
        return stem(material, StyledWindow0SharedTextures.PRIMARY_SLOT);
    }

    /**
     * 物品栏 / 手持 GeckoLib 预览用：第 {@code material} 槽对应的 PNG stem，使 9 种物品图标与 9 张贴图区分。
     */
    public static String itemPreviewStem(int material) {
        return stem(material, material);
    }
}

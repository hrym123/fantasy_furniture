package org.lanye.fantasy_furniture.content.furniture.decor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lanye.fantasy_furniture.content.furniture.common.state.PlainGlassWindowMaterialVariant;

/**
 * 普通玻璃窗「材质」：与 {@link PlainGlassWindowSharedTextures#SLOT_COUNT}（当前 9）一一对应，创造栏物品 id 为
 * {@code plain_glass_window_<颜色后缀>}（见 {@link PlainGlassWindowMaterialVariant}）；方块 {@code material} 为同名枚举。
 *
 * <p>每套 {@link #stem(int, int)} 当前均为 {@link PlainGlassWindowSharedTextures#TEXTURE_STEMS} 的拷贝；世界中与手持预览均用
 * {@link #itemPreviewStem(int)}（第 {@code material} 槽对应 PNG）。若日后为每种材质导出**整套**不同 9 槽文件，替换
 * {@link #STEM_SETS} 中对应行即可。
 *
 * <p>须与 {@code blockstates/plain_glass_window.json} 中 {@code facing}×{@code shape}×{@code material} 全组合一致。
 */
public final class PlainGlassWindowMaterials {

    private static final List<String[]> STEM_SETS = buildStemSets();

    private static List<String[]> buildStemSets() {
        String[] src = PlainGlassWindowSharedTextures.TEXTURE_STEMS;
        int n = PlainGlassWindowSharedTextures.SLOT_COUNT;
        List<String[]> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(Arrays.copyOf(src, src.length));
        }
        return List.copyOf(list);
    }

    static {
        if (PlainGlassWindowMaterialVariant.values().length != STEM_SETS.size()) {
            throw new IllegalStateException(
                    "PlainGlassWindowMaterials: PlainGlassWindowMaterialVariant count vs STEM_SETS mismatch");
        }
        for (int i = 0; i < STEM_SETS.size(); i++) {
            if (STEM_SETS.get(i).length != PlainGlassWindowSharedTextures.SLOT_COUNT) {
                throw new IllegalStateException("PlainGlassWindowMaterials: stem set " + i + " length mismatch");
            }
        }
    }

    private PlainGlassWindowMaterials() {}

    public static int count() {
        return STEM_SETS.size();
    }

    /** 物品注册名与 {@link PlainGlassWindowMaterialVariant} 序列化名一致。 */
    public static String itemSuffix(int material) {
        return PlainGlassWindowMaterialVariant.byIndex(material).getSerializedName();
    }

    public static String stem(int material, int slot) {
        return STEM_SETS.get(material)[slot];
    }

    /** 世界中与 {@link PlainGlassWindowSharedTextures#PRIMARY_SLOT} 对齐的主纹理 stem。 */
    public static String primaryStem(int material) {
        return stem(material, PlainGlassWindowSharedTextures.PRIMARY_SLOT);
    }

    /**
     * 物品栏 / 手持 GeckoLib 预览用：第 {@code material} 槽对应的 PNG stem，使 9 种物品图标与 9 张贴图区分。
     */
    public static String itemPreviewStem(int material) {
        return stem(material, material);
    }
}

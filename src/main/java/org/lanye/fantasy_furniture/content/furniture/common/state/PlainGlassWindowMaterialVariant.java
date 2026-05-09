package org.lanye.fantasy_furniture.content.furniture.common.state;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

/**
 * 普通玻璃窗 {@code material} BlockState 与物品 id 后缀；顺序须与
 * {@link org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowSharedTextures#TEXTURE_STEMS} 槽位 0..n 一致。
 *
 * <p>序列化名为英文颜色（及下划线词），重复主色（如多档 cream）用 {@code cream_b}、{@code cream_c} 区分，避免用槽位数字作 id。
 */
public enum PlainGlassWindowMaterialVariant implements StringRepresentable {
    WHITE("white"),
    BLACK("black"),
    TAN("tan"),
    ICE_BLUE("ice_blue"),
    CREAM("cream"),
    PALE_GREEN("pale_green"),
    CREAM_B("cream_b"),
    MIXED("mixed"),
    CREAM_C("cream_c");

    private final String id;

    PlainGlassWindowMaterialVariant(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    /** 与 {@link org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowSharedTextures#TEXTURE_STEMS} 索引一致。 */
    public static PlainGlassWindowMaterialVariant byIndex(int material) {
        PlainGlassWindowMaterialVariant[] v = values();
        return v[Mth.clamp(material, 0, v.length - 1)];
    }
}

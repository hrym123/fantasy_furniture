package org.lanye.fantasy_furniture.content.furniture.common.state;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

/**
 * 0号窗户颜色后缀与贴图槽序；注册 id 为 {@code styled_window_0_<色>}。顺序须与
 * {@link org.lanye.fantasy_furniture.content.furniture.decor.StyledWindow0SharedTextures#TEXTURE_STEMS} 槽位 0..n 一致。
 *
 * <p>REG-608：每种颜色一对 block/item 同 id，不再作为 BlockState {@code material} 轴。
 */
public enum StyledWindow0MaterialVariant implements StringRepresentable {
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

    StyledWindow0MaterialVariant(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    /** 与 {@link org.lanye.fantasy_furniture.content.furniture.decor.StyledWindow0SharedTextures#TEXTURE_STEMS} 索引一致。 */
    public static StyledWindow0MaterialVariant byIndex(int material) {
        StyledWindow0MaterialVariant[] v = values();
        return v[Mth.clamp(material, 0, v.length - 1)];
    }
}

package org.lanye.fantasy_furniture.client.renderer;

import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.client.model.ArcaneWandItemModel;
import org.lanye.fantasy_furniture.item.ArcaneWandItem;

/**
 * 魔杖专用配置：仅声明 shell 骨骼，其余双 Pass 逻辑复用 {@link SplitPassGeoItemRenderer}。
 *
 * <p>当前模型 `shell` 使用负尺寸立方体，配合 {@link RenderType#entityTranslucentCull} 呈现
 * 「背面渲染、前面剔除」效果。
 */
@OnlyIn(Dist.CLIENT)
public final class ArcaneWandGeoItemRenderer extends SplitPassGeoItemRenderer<ArcaneWandItem> {
    public static final ArcaneWandGeoItemRenderer INSTANCE = new ArcaneWandGeoItemRenderer();
    private static final Set<String> SHELL_BONES = Set.of("shell");

    private ArcaneWandGeoItemRenderer() {
        super(new ArcaneWandItemModel());
    }

    @Override
    protected Set<String> shellBoneNames() {
        return SHELL_BONES;
    }
}

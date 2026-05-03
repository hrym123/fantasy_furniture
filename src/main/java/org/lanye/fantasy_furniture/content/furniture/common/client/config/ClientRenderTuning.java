package org.lanye.fantasy_furniture.content.furniture.common.client.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客户端几何与渲染调参的集中入口（与 Forge/JSON 模组配置不同，为代码内常量，便于在调整资产枢轴、Bedrock 与原版矩阵差异时一处修改多处生效）。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientRenderTuning {
    private ClientRenderTuning() {}

    /**
     * 卡座拐角块实体渲染：在 {@link software.bernie.geckolib.renderer.GeoBlockRenderer#rotateBlock} 之后对拐角 geo
     * 追加绕 Y 的偏航；需与 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BanquetteBlock} 中碰撞箱旋转约定一致。
     */
    public static final class Banquette {
        private Banquette() {}

        /** 左拼拐角相对基础朝向的附加 Y 旋转（度）。 */
        public static final float CORNER_YAW_LEFT_DEG = -90f;

        /** 右拼相对左拼的附加 Y 旋转差（度），即右拼 = {@link #CORNER_YAW_LEFT_DEG} + 该值。 */
        public static final float CORNER_YAW_RIGHT_OFFSET_DEG = 90f;
    }
}

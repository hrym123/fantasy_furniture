package org.lanye.fantasy_furniture.client.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客户端几何与渲染调参的集中入口（与 Forge/JSON 模组配置不同，为代码内常量，便于在调整资产枢轴、Bedrock 与原版矩阵差异时一处修改多处生效）。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientRenderTuning {
    private ClientRenderTuning() {}

    /**
     * 装饰性头盔 Geo 在 {@link net.minecraft.world.item.ItemDisplayContext#HEAD} 下相对原版/Gecko 默认姿态的纠偏，需与 .geo 枢轴和模型朝上一致调参。
     */
    public static final class DecorativeHelmet {
        private DecorativeHelmet() {}

        /**
         * 非 {@link net.minecraft.world.item.ItemDisplayContext#HEAD} 时 Gecko 物品默认的地面枢轴平移（与
         * {@code translate(0.5, 0.51, 0.5)} 习惯一致，HEAD 分支则跳过由头盔矩阵单独处理）。
         */
        public static final float ITEM_GROUND_PIVOT_X = 0.5f;
        public static final float ITEM_GROUND_PIVOT_Y = 0.51f;
        public static final float ITEM_GROUND_PIVOT_Z = 0.5f;

        /** Bedrock 礼帽相对原版头戴附着：上下颠倒时绕 X 轴翻半圈。 */
        public static final float HEAD_ORBIT_X_DEG = 180f;

        /**
         * 头戴时绕局部 Y 轴旋转（度），用于前后朝向与 Bedrock 导出轴向不一致时的纠偏；180° 等价于前后对调。
         */
        public static final float HEAD_ORBIT_Y_DEG = 180f;

        /**
         * 头戴时在局部 X 上的缩放；{@code 1} 表示不镜像（与 Bedrock 导出左右一致）。仅在头戴下左右反了、且不宜改 geo 时改为
         * {@code -1}；非均匀负缩放须在 {@link org.lanye.fantasy_furniture.client.renderer.DecorativeHelmetGeoItemRenderer} 中重算法线。
         */
        public static final float HEAD_MIRROR_X_SCALE = 1f;

        /** 翻转后沿世界 Y 微调，使帽冠对齐颅顶（方块坐标量级，需与 geo 枢轴一致）。 */
        public static final float HEAD_NUDGE_Y = 0;
    }

    /**
     * 卡座拐角块实体渲染：在 {@link software.bernie.geckolib.renderer.GeoBlockRenderer#rotateBlock} 之后对拐角 geo
     * 追加绕 Y 的偏航；需与 {@link org.lanye.fantasy_furniture.block.facing.BanquetteBlock} 中碰撞箱旋转约定一致。
     */
    public static final class Banquette {
        private Banquette() {}

        /** 左拼拐角相对基础朝向的附加 Y 旋转（度）。 */
        public static final float CORNER_YAW_LEFT_DEG = -90f;

        /** 右拼相对左拼的附加 Y 旋转差（度），即右拼 = {@link #CORNER_YAW_LEFT_DEG} + 该值。 */
        public static final float CORNER_YAW_RIGHT_OFFSET_DEG = 90f;
    }
}

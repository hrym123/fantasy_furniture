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

        /** Bedrock 礼帽相对原版头戴附着：上下颠倒时绕 X 轴翻半圈。 */
        public static final float HEAD_ORBIT_X_DEG = 180f;

        /** 翻转后沿世界 Y 微调，使帽冠对齐颅顶（方块坐标量级，需与 geo 枢轴一致）。 */
        public static final float HEAD_NUDGE_Y = -0.3f;
    }
}

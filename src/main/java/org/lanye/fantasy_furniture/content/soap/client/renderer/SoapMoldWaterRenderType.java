package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 肥皂模具水面：entityTranslucent 管线，仅写颜色不写深度（C013 §5.4）。
 *
 * <p>须在 {@link SoapMoldWaterOverlayPass} 专用阶段 immediate {@code endBatch}，勿在 BER 延迟 buffer 中 flush。
 */
@OnlyIn(Dist.CLIENT)
final class SoapMoldWaterRenderType {

    private static final RenderStateShard.ShaderStateShard ENTITY_TRANSLUCENT_SHADER =
            new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader);

    private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard(
                    "fantasy_furniture_soap_mold_water_translucent",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                    },
                    () -> RenderSystem.disableBlend());

    private static final RenderStateShard.LightmapStateShard LIGHTMAP =
            new RenderStateShard.LightmapStateShard(true);

    private static final RenderStateShard.OverlayStateShard NO_OVERLAY =
            new RenderStateShard.OverlayStateShard(false);

    private static final RenderStateShard.CullStateShard NO_CULL =
            new RenderStateShard.CullStateShard(false);

    private static final RenderStateShard.WriteMaskStateShard COLOR_NO_DEPTH =
            new RenderStateShard.WriteMaskStateShard(true, false);

    private static final RenderType WATER_SURFACE =
            RenderType.create(
                    "fantasy_furniture/soap_mold_water",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(ENTITY_TRANSLUCENT_SHADER)
                            .setTextureState(
                                    new RenderStateShard.TextureStateShard(
                                            InventoryMenu.BLOCK_ATLAS, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(NO_OVERLAY)
                            .setCullState(NO_CULL)
                            .setWriteMaskState(COLOR_NO_DEPTH)
                            .createCompositeState(true));

    private SoapMoldWaterRenderType() {}

    static RenderType waterSurface() {
        return WATER_SURFACE;
    }

    static ResourceLocation blockAtlas() {
        return InventoryMenu.BLOCK_ATLAS;
    }
}

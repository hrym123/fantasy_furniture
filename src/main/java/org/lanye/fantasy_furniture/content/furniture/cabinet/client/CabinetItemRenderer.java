package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetAppearance;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.client.model.CabinetItemGeoModel;
import org.lanye.fantasy_furniture.content.furniture.cabinet.item.CabinetBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class CabinetItemRenderer extends GeoItemRenderer<CabinetBlockItem> {

    private static final ThreadLocal<CabinetAppearance> RENDER_APPEARANCE = new ThreadLocal<>();
    private static final ThreadLocal<CabinetKind> RENDER_KIND = new ThreadLocal<>();

    public CabinetItemRenderer() {
        super(new CabinetItemGeoModel());
    }

    public static CabinetAppearance currentAppearance() {
        return RENDER_APPEARANCE.get();
    }

    public static CabinetKind currentKind() {
        return RENDER_KIND.get();
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        CabinetKind kind =
                stack.getItem() instanceof CabinetBlockItem cabinetItem
                        ? cabinetItem.kind()
                        : CabinetKind.CABINET_1;
        RENDER_KIND.set(kind);
        RENDER_APPEARANCE.set(CabinetAppearance.fromStack(stack, kind));
        try {
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            RENDER_APPEARANCE.remove();
            RENDER_KIND.remove();
        }
    }
}

package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapBarGeoBlockRenderer;

@OnlyIn(Dist.CLIENT)
public final class SoapBarClient {

    private SoapBarClient() {}

    public static BlockEntityRendererProvider<SoapBarBlockEntity> blockRendererProvider() {
        return ctx -> new SoapBarGeoBlockRenderer();
    }
}

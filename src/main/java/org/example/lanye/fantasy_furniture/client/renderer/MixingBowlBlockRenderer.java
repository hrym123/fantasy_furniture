package org.example.lanye.fantasy_furniture.client.renderer;

import org.example.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;
import org.example.lanye.fantasy_furniture.client.model.MixingBowlModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 搅拌碗 {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer}。
 */
public class MixingBowlBlockRenderer extends GeoBlockRenderer<MixingBowlBlockEntity> {

    public MixingBowlBlockRenderer() {
        super(new MixingBowlModel());
    }
}

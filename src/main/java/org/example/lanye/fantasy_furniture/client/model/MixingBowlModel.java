package org.example.lanye.fantasy_furniture.client.model;

import net.minecraft.resources.ResourceLocation;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;
import org.example.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

/**
 * 搅拌碗 Geo 模型：资源基名为 {@code mixing_bowl}；{@link DefaultedBlockGeoModel} 约定为
 * {@code geo/block/mixing_bowl.geo.json}、{@code animations/block/mixing_bowl.animation.json}、{@code textures/block/mixing_bowl.png}。
 */
public class MixingBowlModel extends DefaultedBlockGeoModel<MixingBowlBlockEntity> {

    public MixingBowlModel() {
        super(ResourceLocation.fromNamespaceAndPath(Fantasy_furniture.MODID, "mixing_bowl"));
    }
}

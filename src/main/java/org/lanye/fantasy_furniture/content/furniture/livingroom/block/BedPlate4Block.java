package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;

/** 床板4型空床体：碰撞按 {@code bed_plate4.geo.json} 外接轮廓。 */
public final class BedPlate4Block extends BedPlateBlock {

    public BedPlate4Block(
            BlockBehaviour.Properties properties,
            BlockEntityType.BlockEntitySupplier<? extends BedPlateBaseBlockEntity> entitySupplier) {
        super(properties, entitySupplier);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BedPlateEmptyBedCollision.shapeFor(
                state, BedPlateEmptyBedCollision.PLATE4_FOOT, BedPlateEmptyBedCollision.PLATE4_HEAD);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}

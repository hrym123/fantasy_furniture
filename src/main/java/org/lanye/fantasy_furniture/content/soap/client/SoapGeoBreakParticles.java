package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.SoapBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapStackCollisionShapes;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBoxBlock;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** Geo 肥皂盒 / 包装盒摞：按材质贴图喷破坏粒子；摞体只用单层碰撞范围采样，避免粒子过多。 */
@OnlyIn(Dist.CLIENT)
public final class SoapGeoBreakParticles {

    private static final int MIN_COUNT = 4;
    private static final int EXTRA_COUNT = 4;

    private SoapGeoBreakParticles() {}

    public static void forSoapBox(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof SoapBoxBlock) || !(level instanceof ClientLevel clientLevel)) {
            return;
        }
        SoapBoxAppearance appearance = SoapBoxAppearance.fromState(state);
        boolean open = state.getValue(SoapBoxBlock.OPEN);
        ResourceLocation spriteId =
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "block/" + appearance.boxTextureBasename(open));
        VoxelShape shape = state.getShape(level, pos);
        spawn(clientLevel, pos, shape, spriteId, state, manager);
    }

    public static void forSoapPaperBox(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof SoapPaperBoxBlock block)
                || !(level instanceof ClientLevel clientLevel)) {
            return;
        }
        SoapPaperBoxAppearance appearance = SoapPaperBoxAppearance.fromState(state);
        ResourceLocation spriteId =
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID,
                        "block/soap_paper_box_" + appearance.materialId());
        int stackStyle = state.getValue(block.STACK_STYLE);
        VoxelShape north = SoapStackCollisionShapes.soapPaperBoxNorth(1, stackStyle);
        VoxelShape shape =
                VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                        north, state.getValue(block.FACING));
        spawn(clientLevel, pos, shape, spriteId, state, manager);
    }

    private static TextureAtlasSprite resolveBlockSprite(ResourceLocation spriteId, BlockState state) {
        TextureAtlas atlas =
                Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(spriteId);
        if (!isMissingSprite(sprite)) {
            return sprite;
        }
        BlockModelShaper shaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        BakedModel model = shaper.getBlockModel(state);
        return model.getParticleIcon();
    }

    private static boolean isMissingSprite(TextureAtlasSprite sprite) {
        return MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
    }

    private static void spawn(
            ClientLevel level,
            BlockPos pos,
            VoxelShape shape,
            ResourceLocation spriteId,
            BlockState state,
            ParticleEngine manager) {
        if (shape.isEmpty()) {
            return;
        }
        TextureAtlasSprite sprite = resolveBlockSprite(spriteId, state);
        RandomSource random = level.getRandom();
        int count = MIN_COUNT + random.nextInt(EXTRA_COUNT);
        double minX = pos.getX() + shape.min(Direction.Axis.X);
        double minY = pos.getY() + shape.min(Direction.Axis.Y);
        double minZ = pos.getZ() + shape.min(Direction.Axis.Z);
        double sizeX = shape.max(Direction.Axis.X) - shape.min(Direction.Axis.X);
        double sizeY = shape.max(Direction.Axis.Y) - shape.min(Direction.Axis.Y);
        double sizeZ = shape.max(Direction.Axis.Z) - shape.min(Direction.Axis.Z);
        BlockState marker = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < count; i++) {
            double x = minX + random.nextDouble() * sizeX;
            double y = minY + random.nextDouble() * sizeY;
            double z = minZ + random.nextDouble() * sizeZ;
            manager.add(
                    new TerrainParticle(
                            level,
                            x,
                            y,
                            z,
                            random.nextGaussian() * 0.15D,
                            random.nextGaussian() * 0.15D,
                            random.nextGaussian() * 0.15D,
                            marker) {
                        {
                            setSprite(sprite);
                        }
                    });
        }
    }
}

package org.lanye.fantasy_furniture.content.soap.client;

import javax.annotation.Nullable;
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
        ResourceLocation blockSpriteId =
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "block/soap_paper_box_" + appearance.materialId());
        // 物品栏 UI 必进方块图集；作 block 贴图未 stitch 时的兜底，避免紫黑缺失粒子
        ResourceLocation itemUiSpriteId =
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "item/" + appearance.itemUiTextureBasename());
        int stackStyle = state.getValue(block.STACK_STYLE);
        VoxelShape north = SoapStackCollisionShapes.soapPaperBoxNorth(1, stackStyle);
        VoxelShape shape =
                VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                        north, state.getValue(block.FACING));
        spawn(clientLevel, pos, shape, blockSpriteId, itemUiSpriteId, state, manager);
    }

    private static TextureAtlasSprite resolveBlockSprite(
            ResourceLocation spriteId, @Nullable ResourceLocation fallbackSpriteId, BlockState state) {
        TextureAtlas atlas =
                Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(spriteId);
        if (!isMissingSprite(sprite)) {
            return sprite;
        }
        if (fallbackSpriteId != null) {
            sprite = atlas.getSprite(fallbackSpriteId);
            if (!isMissingSprite(sprite)) {
                return sprite;
            }
        }
        BlockModelShaper shaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        BakedModel model = shaper.getBlockModel(state);
        return model.getParticleIcon();
    }

    private static boolean isMissingSprite(TextureAtlasSprite sprite) {
        return sprite == null
                || MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
    }

    private static void spawn(
            ClientLevel level,
            BlockPos pos,
            VoxelShape shape,
            ResourceLocation spriteId,
            BlockState state,
            ParticleEngine manager) {
        spawn(level, pos, shape, spriteId, null, state, manager);
    }

    private static void spawn(
            ClientLevel level,
            BlockPos pos,
            VoxelShape shape,
            ResourceLocation spriteId,
            @Nullable ResourceLocation fallbackSpriteId,
            BlockState state,
            ParticleEngine manager) {
        if (shape.isEmpty()) {
            return;
        }
        TextureAtlasSprite sprite = resolveBlockSprite(spriteId, fallbackSpriteId, state);
        RandomSource random = level.getRandom();
        int count = MIN_COUNT + random.nextInt(EXTRA_COUNT);
        double minX = pos.getX() + shape.min(Direction.Axis.X);
        double minY = pos.getY() + shape.min(Direction.Axis.Y);
        double minZ = pos.getZ() + shape.min(Direction.Axis.Z);
        double sizeX = shape.max(Direction.Axis.X) - shape.min(Direction.Axis.X);
        double sizeY = shape.max(Direction.Axis.Y) - shape.min(Direction.Axis.Y);
        double sizeZ = shape.max(Direction.Axis.Z) - shape.min(Direction.Axis.Z);
        for (int i = 0; i < count; i++) {
            double x = minX + random.nextDouble() * sizeX;
            double y = minY + random.nextDouble() * sizeY;
            double z = minZ + random.nextDouble() * sizeZ;
            // 用真实方块状态构造，再覆盖 sprite；避免 AIR 粒子图标为 missingno
            manager.add(
                    new TerrainParticle(
                            level,
                            x,
                            y,
                            z,
                            random.nextGaussian() * 0.15D,
                            random.nextGaussian() * 0.15D,
                            random.nextGaussian() * 0.15D,
                            state) {
                        {
                            setSprite(sprite);
                        }
                    });
        }
    }
}

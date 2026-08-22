package org.lanye.fantasy_furniture.content.furniture.decor.series;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

@OnlyIn(Dist.CLIENT)
public final class StyledWindowSeriesBreakParticles {

    private static final int MIN_COUNT = 4;
    private static final int EXTRA_COUNT = 4;

    private StyledWindowSeriesBreakParticles() {}

    public static void spawn(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof StyledWindowSeriesBlock block)
                || !(level instanceof ClientLevel clientLevel)) {
            return;
        }
        String stem = block.spec().textureStem(block.colorIndex());
        ResourceLocation spriteId =
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "block/" + stem);
        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) {
            return;
        }
        TextureAtlas atlas =
                Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(spriteId);
        if (sprite == null
                || MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
            sprite =
                    Minecraft.getInstance()
                            .getBlockRenderer()
                            .getBlockModelShaper()
                            .getBlockModel(state)
                            .getParticleIcon();
        }
        RandomSource random = clientLevel.getRandom();
        int count = MIN_COUNT + random.nextInt(EXTRA_COUNT);
        double minX = pos.getX() + shape.min(Direction.Axis.X);
        double minY = pos.getY() + shape.min(Direction.Axis.Y);
        double minZ = pos.getZ() + shape.min(Direction.Axis.Z);
        double sizeX = shape.max(Direction.Axis.X) - shape.min(Direction.Axis.X);
        double sizeY = shape.max(Direction.Axis.Y) - shape.min(Direction.Axis.Y);
        double sizeZ = shape.max(Direction.Axis.Z) - shape.min(Direction.Axis.Z);
        TextureAtlasSprite finalSprite = sprite;
        for (int i = 0; i < count; i++) {
            double x = minX + random.nextDouble() * sizeX;
            double y = minY + random.nextDouble() * sizeY;
            double z = minZ + random.nextDouble() * sizeZ;
            manager.add(
                    new TerrainParticle(
                            clientLevel,
                            x,
                            y,
                            z,
                            random.nextGaussian() * 0.15D,
                            random.nextGaussian() * 0.15D,
                            random.nextGaussian() * 0.15D,
                            state) {
                        {
                            setSprite(finalSprite);
                        }
                    });
        }
    }
}

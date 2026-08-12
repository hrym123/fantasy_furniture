package org.lanye.fantasy_furniture.content.soap.client;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
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
import org.lanye.fantasy_furniture.content.soap.BodyCreamAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyWashAppearance;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAppearance;
import org.lanye.fantasy_furniture.content.soap.ShampooAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapStackCollisionShapes;
import org.lanye.fantasy_furniture.content.soap.block.BodyCreamBlock;
import org.lanye.fantasy_furniture.content.soap.block.BodyWashBlock;
import org.lanye.fantasy_furniture.content.soap.block.DisplayCabinetBlock;
import org.lanye.fantasy_furniture.content.soap.block.ShampooBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBagBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBoxBlock;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * Geo 肥皂系：按材质贴图喷破坏粒子（模型 JSON 写死 {@code *_1} 时默认粒子色错误）。
 * 摞体只用单层碰撞范围采样，避免粒子过多。
 *
 * <p>使用 {@link SheetBreakParticle} 而非 {@code TerrainParticle(BlockState)}：后者会绑模型
 * {@code particle}（常写死默认色）。
 */
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

    public static void forSoapBar(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof SoapBarBlock) || !(level instanceof ClientLevel clientLevel)) {
            return;
        }
        SoapBarAppearance appearance = SoapBarAppearance.fromState(state);
        String blockBasename =
                appearance.isPackaged()
                        ? appearance.packagingTextureBasename()
                        : appearance.textureBasename();
        ResourceLocation blockSpriteId =
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "block/" + blockBasename);
        ResourceLocation itemUiSpriteId;
        if (appearance.isBoxed()) {
            itemUiSpriteId =
                    ResourceLocation.fromNamespaceAndPath(
                            FantasyFurniture.MODID, "item/soap_paper_box_ui_" + appearance.boxMaterialId());
        } else if (appearance.isPackaged()) {
            itemUiSpriteId =
                    ResourceLocation.fromNamespaceAndPath(
                            FantasyFurniture.MODID, "item/soap_paper_bag_ui_" + appearance.bagMaterialId());
        } else {
            itemUiSpriteId =
                    ResourceLocation.fromNamespaceAndPath(
                            FantasyFurniture.MODID, "item/" + appearance.itemUiTextureBasename());
        }
        VoxelShape shape = state.getShape(level, pos);
        spawn(clientLevel, pos, shape, blockSpriteId, itemUiSpriteId, state, manager);
    }

    public static void forSoapPaperBag(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof SoapPaperBagBlock block)
                || !(level instanceof ClientLevel clientLevel)) {
            return;
        }
        SoapPaperBagAppearance appearance = SoapPaperBagAppearance.fromState(state);
        boolean stacked = state.getValue(block.LAYERS) > 1;
        String blockBasename =
                stacked ? appearance.stackTextureBasename() : appearance.handheldTextureBasename();
        ResourceLocation blockSpriteId =
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "block/" + blockBasename);
        ResourceLocation itemUiSpriteId =
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "item/" + appearance.itemUiTextureBasename());
        VoxelShape north = SoapStackCollisionShapes.soapPaperBagNorth(1);
        VoxelShape shape =
                VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                        north, state.getValue(block.FACING));
        spawn(clientLevel, pos, shape, blockSpriteId, itemUiSpriteId, state, manager);
    }

    public static void forBodyWash(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof BodyWashBlock block)
                || !(level instanceof ClientLevel clientLevel)) {
            return;
        }
        BodyWashAppearance appearance = new BodyWashAppearance(state.getValue(block.MATERIAL));
        ResourceLocation blockSpriteId = blockAtlasSpriteFromTexture(appearance.textureLocation());
        VoxelShape north = SoapStackCollisionShapes.bodyWashNorth(1);
        VoxelShape shape =
                VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                        north, state.getValue(block.FACING));
        spawn(clientLevel, pos, shape, blockSpriteId, state, manager);
    }

    public static void forShampoo(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof ShampooBlock block)
                || !(level instanceof ClientLevel clientLevel)) {
            return;
        }
        ShampooAppearance appearance = new ShampooAppearance(state.getValue(block.MATERIAL));
        ResourceLocation blockSpriteId = blockAtlasSpriteFromTexture(appearance.textureLocation());
        VoxelShape north = SoapStackCollisionShapes.shampooNorth(1);
        VoxelShape shape =
                VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                        north, state.getValue(block.FACING));
        spawn(clientLevel, pos, shape, blockSpriteId, state, manager);
    }

    public static void forBodyCream(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof BodyCreamBlock block)
                || !(level instanceof ClientLevel clientLevel)) {
            return;
        }
        BodyCreamAppearance appearance = new BodyCreamAppearance(state.getValue(block.MATERIAL));
        ResourceLocation blockSpriteId = blockAtlasSpriteFromTexture(appearance.textureLocation());
        VoxelShape north = SoapStackCollisionShapes.bodyCreamNorth(1);
        VoxelShape shape =
                VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                        north, state.getValue(block.FACING));
        spawn(clientLevel, pos, shape, blockSpriteId, state, manager);
    }

    public static void forDisplayCabinet(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(state.getBlock() instanceof DisplayCabinetBlock)
                || !(level instanceof ClientLevel clientLevel)) {
            return;
        }
        DisplayCabinetAppearance appearance = DisplayCabinetAppearance.fromState(state);
        boolean open = state.getValue(DisplayCabinetBlock.OPEN);
        ResourceLocation blockSpriteId =
                blockAtlasSpriteFromTexture(appearance.textureLocation(open));
        VoxelShape shape = state.getShape(level, pos);
        spawn(clientLevel, pos, shape, blockSpriteId, state, manager);
    }

    /** {@code textures/block/foo.png} → 方块图集 id {@code block/foo}。 */
    private static ResourceLocation blockAtlasSpriteFromTexture(ResourceLocation textureLocation) {
        String path = textureLocation.getPath();
        if (path.startsWith("textures/") && path.endsWith(".png")) {
            path = path.substring("textures/".length(), path.length() - ".png".length());
        }
        return ResourceLocation.fromNamespaceAndPath(textureLocation.getNamespace(), path);
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
            manager.add(
                    new SheetBreakParticle(
                            level,
                            x,
                            y,
                            z,
                            random.nextGaussian() * 0.15D,
                            random.nextGaussian() * 0.15D,
                            random.nextGaussian() * 0.15D,
                            sprite));
        }
    }

    /** 破坏碎屑：固定贴图精灵，不读方块模型 particle。 */
    @OnlyIn(Dist.CLIENT)
    private static final class SheetBreakParticle extends TextureSheetParticle {

        private SheetBreakParticle(
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd,
                TextureAtlasSprite sprite) {
            super(level, x, y, z, xd, yd, zd);
            setSprite(sprite);
            this.gravity = 1.0F;
            this.rCol = 0.6F;
            this.gCol = 0.6F;
            this.bCol = 0.6F;
            this.quadSize *= 0.5F;
            this.lifetime = Math.max(1, (int) (16.0D / (this.random.nextFloat() * 0.9D + 0.1D)));
            this.hasPhysics = true;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.TERRAIN_SHEET;
        }
    }
}

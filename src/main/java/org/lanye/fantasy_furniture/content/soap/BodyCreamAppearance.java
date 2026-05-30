package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.block.BodyCreamBlock;

/** 乳霜外观：颜料存 NBT / 方块状态 {@link BodyCreamBlock#MATERIAL}。 */
public record BodyCreamAppearance(int materialId) {

    private static final String NBT_CREAM_MAT = "CreamMat";

    public BodyCreamAppearance {
        if (!BodyCreamMaterials.isValid(materialId)) {
            materialId = BodyCreamMaterials.DEFAULT;
        }
    }

    public static BodyCreamAppearance defaults() {
        return new BodyCreamAppearance(BodyCreamMaterials.DEFAULT);
    }

    public ResourceLocation textureLocation() {
        return BodyCreamAssets.textureLocation(materialId);
    }

    public static BodyCreamAppearance fromState(BlockState state) {
        if (state.getBlock() instanceof BodyCreamBlock block) {
            return new BodyCreamAppearance(state.getValue(block.MATERIAL));
        }
        return defaults();
    }

    public static BodyCreamAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_CREAM_MAT)) {
            return defaults();
        }
        return new BodyCreamAppearance(tag.getInt(NBT_CREAM_MAT));
    }

    public static void writeToStack(ItemStack stack, BodyCreamAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_CREAM_MAT, appearance.materialId());
    }
}

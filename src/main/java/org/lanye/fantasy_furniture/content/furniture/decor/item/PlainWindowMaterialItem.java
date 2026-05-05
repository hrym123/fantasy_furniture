package org.lanye.fantasy_furniture.content.furniture.decor.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.bootstrap.block.PlainWindowBlocks;
import org.lanye.fantasy_furniture.bootstrap.block.PlainWindowBlocks.Material;
import org.lanye.fantasy_furniture.bootstrap.block.PlainWindowBlocks.Shape;

/**
 * 按颜色材质折叠的普通窗户物品：创造栏每种颜色一条；造型由物品 NBT（战利品/中键）决定；对已放置方块潜行右键切换见 {@link
 * org.lanye.fantasy_furniture.content.furniture.decor.block.PlainWindowBlock}。
 */
public class PlainWindowMaterialItem extends BlockItem {

    public static final String TAG_SHAPE = "FFShape";

    private final Material material;

    public PlainWindowMaterialItem(Block defaultShapeBlock, Material material, Properties properties) {
        super(defaultShapeBlock, properties);
        this.material = material;
    }

    public Material material() {
        return material;
    }

    public static Shape getShape(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_SHAPE)) {
            return Shape.DEFAULT;
        }
        return Shape.fromId(tag.getString(TAG_SHAPE));
    }

    public static void setShape(ItemStack stack, Shape shape) {
        stack.getOrCreateTag().putString(TAG_SHAPE, shape.id);
    }

    public static ItemStack createStack(Material material, Shape shape) {
        ItemStack stack = new ItemStack(PlainWindowBlocks.itemFor(material).get());
        setShape(stack, shape);
        return stack;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Block toPlace = PlainWindowBlocks.blockFor(material, getShape(context.getItemInHand()));
        return toPlace.getStateForPlacement(context);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Shape s = getShape(stack);
        tooltip.add(Component.translatable("tooltip.fantasy_furniture.plain_window.shape." + s.id)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.fantasy_furniture.plain_window.cycle_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}

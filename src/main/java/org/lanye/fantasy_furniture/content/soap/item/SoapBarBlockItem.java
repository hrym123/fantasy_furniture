package org.lanye.fantasy_furniture.content.soap.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapBarWear;
import org.lanye.fantasy_furniture.content.soap.client.SoapBarItemRenderer;
import org.lanye.fantasy_furniture.content.soap.effect.BubbleEffectApplier;
import org.lanye.fantasy_furniture.content.soap.effect.BubbleMobEffect;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/**
 * 肥皂物品：单 id，磨损与颜料存于 NBT（{@link SoapBarAppearance}）；物品栏 2D 物品材质，手持 Geo。
 *
 * <p>对空气长按：有包装 → 撕开包装；无包装且蹲下 → 消耗肥皂并 +10 分钟泡泡；无包装站立 → 不消耗、+10 秒泡泡。
 */
public final class SoapBarBlockItem extends GeolibBlockItem {

    public SoapBarBlockItem(Block block, Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public Component getName(ItemStack stack) {
        SoapBarAppearance appearance = SoapBarAppearance.fromStack(stack);
        if (appearance.isBoxed()) {
            return Component.translatable(
                    "item.fantasy_furniture.soap_bar.boxed",
                    Component.translatable(
                            SoapBarMaterials.colorTranslationKey(appearance.boxMaterialId())));
        }
        if (appearance.isBagged()) {
            return Component.translatable(
                    "item.fantasy_furniture.soap_bar.packaged",
                    Component.translatable(SoapBarMaterials.colorTranslationKey(appearance.bagMaterialId())));
        }
        if (appearance.wear() == SoapBarAppearance.DEFAULT_WEAR) {
            return Component.translatable(
                    "item.fantasy_furniture.soap_bar.named_full",
                    Component.translatable(SoapBarMaterials.colorTranslationKey(appearance.materialId())));
        }
        return Component.translatable(
                "item.fantasy_furniture.soap_bar.named_worn",
                Component.translatable(SoapBarMaterials.colorTranslationKey(appearance.materialId())),
                Component.translatable(SoapBarWear.wearTranslationKey(appearance.wear())));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return BubbleMobEffect.USE_TICKS;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(
            @NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (level.isClientSide) {
            return stack;
        }
        SoapBarAppearance appearance = SoapBarAppearance.fromStack(stack);
        if (appearance.isPackaged()) {
            SoapBarAppearance unwrapped =
                    new SoapBarAppearance(
                            appearance.wear(),
                            appearance.materialId(),
                            0,
                            false,
                            appearance.particleMatId(),
                            0);
            SoapBarAppearance.writeToStack(stack, unwrapped);
            return stack;
        }
        if (entity.isCrouching()) {
            BubbleEffectApplier.applyFromConsumedSoap(entity);
            if (entity instanceof Player player && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return stack;
        }
        BubbleEffectApplier.apply(entity);
        return stack;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new SoapBarItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    /** 创造栏 / 指令用：指定颜料与磨损的堆叠。 */
    public static ItemStack stackWithAppearance(Item item, SoapBarAppearance appearance) {
        ItemStack stack = new ItemStack(item);
        SoapBarAppearance.writeToStack(stack, appearance);
        return stack;
    }
}

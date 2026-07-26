package org.lanye.fantasy_furniture.content.tool;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** 线轴换色 HUD：由准心命中层解析「下一档」床品物品与展示名。 */
public final class CoilRecolorPreview {

    public record Preview(ItemStack stack, Component name) {}

    private CoilRecolorPreview() {}

    public static Optional<Preview> forHit(Level level, BlockState state, BlockPos pos, Vec3 hitLocation) {
        return CoilRecolor.previewNextStack(level, state, pos, hitLocation)
                .map(stack -> new Preview(stack, stack.getHoverName()));
    }

    /** 与刷子 HUD 相同截断规则。 */
    public static String truncateDisplayName(String raw) {
        return BrushRecolorPreview.truncateDisplayName(raw);
    }
}

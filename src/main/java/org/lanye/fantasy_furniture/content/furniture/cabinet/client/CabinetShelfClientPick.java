package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.reverie_core.content.fantasy_core.item.FantasyDebugStickItem;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 持幻想调试棒时准心隔板拾取 / 描边形。 */
@OnlyIn(Dist.CLIENT)
public final class CabinetShelfClientPick {

    private CabinetShelfClientPick() {}

    public static boolean holdingDebugStick(@Nullable Player player) {
        return player != null
                && player.getMainHandItem().getItem() instanceof FantasyDebugStickItem;
    }

    /**
     * @return 对准的隔板体素（已按朝向旋转，相对 master 底格），未命中为 null
     */
    @Nullable
    public static VoxelShape aimedShelfShape(Level level, BlockState state, BlockPos hitPos, BlockHitResult bhr) {
        if (!(state.getBlock() instanceof CabinetBlock cabinet)) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (!holdingDebugStick(player)) {
            return null;
        }
        BlockPos master = CabinetBlock.masterPos(state, hitPos);
        BlockEntity raw = level.getBlockEntity(master);
        if (!(raw instanceof CabinetBlockEntity be)) {
            return null;
        }
        CabinetKind kind = cabinet.kind();
        Direction facing = state.getValue(CabinetBlock.FACING);
        boolean restore = player.isShiftKeyDown();
        int shelf = kind.pickShelf(
                master,
                facing,
                player.getEyePosition(1.0f),
                player.getViewVector(1.0f),
                be.shelvesMask(),
                restore);
        if (shelf < 0) {
            return null;
        }
        VoxelShape north = kind.shelfNorthShape(shelf);
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, facing);
    }

    /** 描边时应使用的方块原点（master），形相对该原点。 */
    public static BlockPos outlineOrigin(BlockState state, BlockPos hitPos) {
        return CabinetBlock.masterPos(state, hitPos);
    }
}

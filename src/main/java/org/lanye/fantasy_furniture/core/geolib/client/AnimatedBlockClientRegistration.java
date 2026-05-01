package org.lanye.fantasy_furniture.core.geolib.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import org.lanye.fantasy_furniture.core.geolib.AnimatedBlockEntry;

/**
 * 设计书 §4.1 客户端阶段：将 {@link BlockEntityRendererProvider} 与已注册的 {@link AnimatedBlockEntry} 绑定。
 * <p>
 * 须在客户端类路径上调用 {@link #registerBlockEntityRenderer}（例如 {@code ClientModEvents} 的静态块），
 * 再在 {@link EntityRenderersEvent.RegisterRenderers} 中调用 {@link #registerAllRenderers}。
 */
@OnlyIn(Dist.CLIENT)
public final class AnimatedBlockClientRegistration {

    private static final List<Consumer<EntityRenderersEvent.RegisterRenderers>> REGISTRATIONS = new ArrayList<>();

    private AnimatedBlockClientRegistration() {}

    public static <BE extends BlockEntity> void registerBlockEntityRenderer(
            AnimatedBlockEntry<BE> entry, BlockEntityRendererProvider<BE> provider) {
        REGISTRATIONS.add(event -> event.registerBlockEntityRenderer(entry.blockEntityType().get(), provider));
    }

    public static void registerAllRenderers(EntityRenderersEvent.RegisterRenderers event) {
        REGISTRATIONS.forEach(c -> c.accept(event));
    }
}

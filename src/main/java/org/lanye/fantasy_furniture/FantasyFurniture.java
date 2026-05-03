package org.lanye.fantasy_furniture;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lanye.fantasy_furniture.bootstrap.ModBootstrap;
import org.lanye.fantasy_furniture.bootstrap.entity.ModEntities;
import org.lanye.reverie_core.seat.SeatEntityTypes;
import org.slf4j.Logger;

/**
 * 「幻想家具」模组主入口类。
 * <p>
 * 负责模组级初始化，并在 {@link net.minecraftforge.fml.common.Mod} 标注下由 Forge 加载；
 * {@link #MODID} 须与 {@code META-INF/mods.toml} 及资源命名空间一致。
 */
@Mod(FantasyFurniture.MODID)
public class FantasyFurniture {

    public static final String MODID = "fantasy_furniture";
    private static final Logger LOGGER = LogUtils.getLogger();

    public FantasyFurniture(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        var modEventBus = context.getModEventBus();
        ModBootstrap.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(
                () -> {
                    SeatEntityTypes.bind(() -> ModEntities.FURNITURE_SEAT.get());
                    ModBootstrap.registerCommonWork();
                });
        LOGGER.info("{} 通用初始化完成", MODID);
    }
}

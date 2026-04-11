package org.example.lanye.fantasy_furniture;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.example.lanye.fantasy_furniture.block.ModBlocks;
import org.example.lanye.fantasy_furniture.item.ModCreativeTabs;
import org.example.lanye.fantasy_furniture.item.ModItems;
import org.example.lanye.fantasy_furniture.registry.ModBlockEntities;
import org.example.lanye.fantasy_furniture.registry.ModEntities;
import org.example.lanye.fantasy_furniture.registry.ModSeatConfigs;
import org.slf4j.Logger;

/**
 * 「幻想家具」模组主入口类。
 * <p>
 * 负责模组级初始化，并在 {@link net.minecraftforge.fml.common.Mod} 标注下由 Forge 加载；
 * {@link #MODID} 须与 {@code META-INF/mods.toml} 及资源命名空间一致。
 */
@Mod(Fantasy_furniture.MODID)
public class Fantasy_furniture {

    public static final String MODID = "fantasy_furniture";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Fantasy_furniture(FMLJavaModLoadingContext context) {
        var modEventBus = context.getModEventBus();
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModSeatConfigs::register);
        LOGGER.info("{} 通用初始化完成", MODID);
    }
}

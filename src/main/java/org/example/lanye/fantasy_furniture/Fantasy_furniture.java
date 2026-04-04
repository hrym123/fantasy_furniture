package org.example.lanye.fantasy_furniture;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Fantasy_furniture.MODID)
public class Fantasy_furniture {

    public static final String MODID = "fantasy_furniture";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Fantasy_furniture() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("{} 通用初始化完成", MODID);
    }
}

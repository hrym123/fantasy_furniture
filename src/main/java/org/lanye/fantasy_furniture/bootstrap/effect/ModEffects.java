package org.lanye.fantasy_furniture.bootstrap.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.effect.BubbleMobEffect;

/** 幻想家具状态效果注册。 */
public final class ModEffects {

    private ModEffects() {}

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, FantasyFurniture.MODID);

    /** 泡泡：漂浮 + 耗氧 + 头饰；amplifier 0=兔耳、1=猫耳。 */
    public static final RegistryObject<MobEffect> BUBBLE =
            MOB_EFFECTS.register("bubble", BubbleMobEffect::new);

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}

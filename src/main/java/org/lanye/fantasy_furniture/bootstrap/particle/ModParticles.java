package org.lanye.fantasy_furniture.bootstrap.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapFlatLiquidMaterials;

/** 肥皂入水溶解粒子（六色，对应 moonstarfish {@code 粒子效果/粒子_*.png}）。 */
public final class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, FantasyFurniture.MODID);

    @SuppressWarnings("unchecked")
    private static final RegistryObject<SimpleParticleType>[] SOAP_DISSOLVE = new RegistryObject[7];

    static {
        for (int id = 1; id <= SoapFlatLiquidMaterials.COUNT; id++) {
            SOAP_DISSOLVE[id] =
                    PARTICLE_TYPES.register(
                            "soap_dissolve_" + id, () -> new SimpleParticleType(false));
        }
    }

    private ModParticles() {}

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }

    /** 供客户端注册 Provider。 */
    public static RegistryObject<SimpleParticleType> soapDissolveObject(int particleMatId) {
        if (!SoapFlatLiquidMaterials.isValid(particleMatId)) {
            particleMatId = SoapBarAppearance.DEFAULT_PARTICLE_MAT;
        }
        return SOAP_DISSOLVE[particleMatId];
    }

    public static SimpleParticleType soapDissolve(int particleMatId) {
        return soapDissolveObject(particleMatId).get();
    }
}

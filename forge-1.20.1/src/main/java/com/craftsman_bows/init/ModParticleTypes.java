package com.craftsman_bows.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.craftsman_bows.CraftsmanBows.MOD_ID;

public class ModParticleTypes {

    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MOD_ID);

    public static final RegistryObject<SimpleParticleType> CHARGE_DUST = simple("charge_dust");
    public static final RegistryObject<SimpleParticleType> CHARGE_END = simple("charge_end");
    public static final RegistryObject<SimpleParticleType> SHOOT = simple("shoot");

    private static RegistryObject<SimpleParticleType> simple(String name) {
        // SimpleParticleType のコンストラクタは protected なので無名サブクラスで生成する
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false) {});
    }

    public static void register(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }
}

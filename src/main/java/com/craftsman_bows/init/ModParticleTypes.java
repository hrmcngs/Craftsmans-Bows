package com.craftsman_bows.init;

import net.minecraft.particles.ParticleType;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import static com.craftsman_bows.CraftsmanBows.Mod_ID;

@Mod.EventBusSubscriber(modid = Mod_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Mod_ID);

    @ObjectHolder(Mod_ID + ":charge_dust")
    public static final RegistryObject<ParticleType<?>> CHARGE_DUST = PARTICLES.register("charge_dust", () -> new ParticleType<>(false, ParticleType.Deserializer));

    @ObjectHolder(Mod_ID + ":charge_end")
    public static final RegistryObject<ParticleType<?>> CHARGE_END = PARTICLES.register("charge_end", () -> new ParticleType<>(false, ParticleType.Deserializer));

    @ObjectHolder(Mod_ID + ":shoot")
    public static final RegistryObject<ParticleType<?>> SHOOT = PARTICLES.register("shoot", () -> new ParticleType<>(false, ParticleType.Deserializer));

    @SubscribeEvent
    public static void registerParticles(final RegistryEvent.Register<ParticleType<?>> event) {
        event.getRegistry().registerAll(
            CHARGE_DUST.get(),
            CHARGE_END.get(),
            SHOOT.get()
        );
    }
}

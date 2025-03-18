package com.craftsman_bows.init;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

import static com.craftsman_bows.CraftsmanBows.Mod_ID;

public class ModParticleTypes {

    public static final SimpleParticleType CHARGE_DUST = new SimpleParticleType(false);
    public static final SimpleParticleType CHARGE_END = new SimpleParticleType(false);
    public static final SimpleParticleType SHOOT = new SimpleParticleType(false);

    public static void init() {
        Registry.register(Registry.PARTICLE_TYPE, new ResourceLocation(Mod_ID, "charge_dust"), CHARGE_DUST);
        Registry.register(Registry.PARTICLE_TYPE, new ResourceLocation(Mod_ID, "charge_end"), CHARGE_END);
        Registry.register(Registry.PARTICLE_TYPE, new ResourceLocation(Mod_ID, "shoot"), SHOOT);
    }
}

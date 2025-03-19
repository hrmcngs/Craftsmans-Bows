package com.craftsman_bows;

import com.craftsman_bows.particle.ChargeDustParticle;
import com.craftsman_bows.particle.ChargedParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

import static com.craftsman_bows.init.ModParticleTypes.*;

@Mod.EventBusSubscriber(modid = CraftsmanBows.Mod_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterParticle {

    @ObjectHolder("craftsman_bows:charge_dust")
    public static final ParticleType<ChargeDustParticle> CHARGE_DUST = null;

    @ObjectHolder("craftsman_bows:shoot")
    public static final ParticleType<ChargedParticle> SHOOT = null;

    @ObjectHolder("craftsman_bows:charge_end")
    public static final ParticleType<ChargedParticle> CHARGE_END = null;

    public static void init(FMLClientSetupEvent event) {
        ParticleManager particleManager = Minecraft.getInstance().particles;

        particleManager.registerFactory(CHARGE_DUST, ChargeDustParticle.Factory::new);
        particleManager.registerFactory(SHOOT, ChargedParticle.Factory::new);
        particleManager.registerFactory(CHARGE_END, ChargedParticle.Factory::new);
    }

    public RegisterParticle() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    }
}

package com.craftsman_bows.client;

import com.craftsman_bows.CraftsmanBows;
import com.craftsman_bows.client.particle.ChargeDustParticle;
import com.craftsman_bows.client.particle.ChargedParticle;
import com.craftsman_bows.init.ModItems;
import com.craftsman_bows.init.ModParticleTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CraftsmanBows.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    /** 1.21 版の items/*.json（range_dispatch）に相当するモデル切り替え用プロパティ */
    private static final ResourceLocation PULLING = new ResourceLocation("pulling");
    private static final ResourceLocation USE_TICKS = new ResourceLocation(CraftsmanBows.MOD_ID, "use_ticks");

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.CHARGE_DUST.get(), ChargeDustParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SHOOT.get(), ChargedParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CHARGE_END.get(), ChargedParticle.Provider::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModItems.ALL.forEach(holder -> {
            ItemProperties.register(holder.get(), PULLING, (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

            ItemProperties.register(holder.get(), USE_TICKS, (stack, level, entity, seed) -> {
                if (entity == null || entity.getUseItem() != stack) {
                    return 0.0F;
                }
                return stack.getUseDuration() - entity.getUseItemRemainingTicks();
            });
        }));
    }
}

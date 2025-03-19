package com.craftsman_bows;

import com.craftsman_bows.init.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CraftsmanBows.Mod_ID)
public class CraftsmanBows {
    public static final String Mod_ID = "craftsman_bows";

    public CraftsmanBows() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        item.ITEMS.register(modEventBus);
        ModParticleTypes.PARTICLES.register(modEventBus);
        ModSoundEvents.SOUNDS.register(modEventBus);
        ModComponents.COMPONENTS.register(modEventBus);
    }
}

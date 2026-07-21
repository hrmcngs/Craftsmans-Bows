package com.craftsman_bows;

import com.craftsman_bows.init.ModEnchantments;
import com.craftsman_bows.init.ModItems;
import com.craftsman_bows.init.ModParticleTypes;
import com.craftsman_bows.init.ModSoundEvents;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CraftsmanBows.MOD_ID)
public class CraftsmanBows {

    public static final String MOD_ID = "craftsman_bows";

    public CraftsmanBows() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modBus);
        ModParticleTypes.register(modBus);
        ModSoundEvents.register(modBus);
        ModEnchantments.register(modBus);

        modBus.addListener(ModItems::addToCreativeTab);
    }
}

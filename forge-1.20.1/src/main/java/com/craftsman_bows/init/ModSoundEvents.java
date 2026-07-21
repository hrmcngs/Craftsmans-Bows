package com.craftsman_bows.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.craftsman_bows.CraftsmanBows.MOD_ID;

public class ModSoundEvents {

    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    public static final RegistryObject<SoundEvent> LEGACY_BOW_SHOOT_1 = registerSoundEvent("legacy_arrow_1");
    public static final RegistryObject<SoundEvent> LEGACY_BOW_SHOOT_2 = registerSoundEvent("legacy_arrow_2");

    public static final RegistryObject<SoundEvent> DUNGEONS_BOW_CHARGE_1 = registerSoundEvent("dungeons_bow_charge_1");
    public static final RegistryObject<SoundEvent> DUNGEONS_BOW_CHARGE_2 = registerSoundEvent("dungeons_bow_charge_2");
    public static final RegistryObject<SoundEvent> DUNGEONS_BOW_CHARGE_3 = registerSoundEvent("dungeons_bow_charge_3");
    public static final RegistryObject<SoundEvent> DUNGEONS_BOW_CHARGE_4 = registerSoundEvent("dungeons_bow_charge_4");

    public static final RegistryObject<SoundEvent> DUNGEONS_BOW_LOAD = registerSoundEvent("dungeons_bow_load");
    public static final RegistryObject<SoundEvent> DUNGEONS_BOW_SHOOT = registerSoundEvent("dungeons_bow_shoot");
    public static final RegistryObject<SoundEvent> DUNGEONS_COG_CROSSBOW_PICKUP = registerSoundEvent("dungeons_cog_crossbow_pickup");
    public static final RegistryObject<SoundEvent> DUNGEONS_COG_CROSSBOW_SHOOT = registerSoundEvent("dungeons_cog_crossbow_shoot");
    public static final RegistryObject<SoundEvent> DUNGEONS_COG_CROSSBOW_PLACE = registerSoundEvent("dungeons_cog_crossbow_place");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, name)));
    }

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }
}

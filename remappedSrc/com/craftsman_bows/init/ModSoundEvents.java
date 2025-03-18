package com.craftsman_bows.init;

import com.craftsman_bows.CraftsmanBows;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSoundEvents {

    public static final SoundEvent LEGACY_BOW_SHOOT_1 = registerSoundEvent("legacy_arrow_1");
    public static final SoundEvent LEGACY_BOW_SHOOT_2 = registerSoundEvent("legacy_arrow_2");

    public static final SoundEvent DUNGEONS_BOW_CHARGE_1 = registerSoundEvent("dungeons_bow_charge_1");
    public static final SoundEvent DUNGEONS_BOW_CHARGE_2 = registerSoundEvent("dungeons_bow_charge_2");
    public static final SoundEvent DUNGEONS_BOW_CHARGE_3 = registerSoundEvent("dungeons_bow_charge_3");
    public static final SoundEvent DUNGEONS_BOW_CHARGE_4 = registerSoundEvent("dungeons_bow_charge_4");

    public static final SoundEvent DUNGEONS_BOW_LOAD = registerSoundEvent("dungeons_bow_load");
    public static final SoundEvent DUNGEONS_BOW_SHOOT = registerSoundEvent("dungeons_bow_shoot");
    public static final SoundEvent DUNGEONS_COG_CROSSBOW_PICKUP = registerSoundEvent("dungeons_cog_crossbow_pickup");
    public static final SoundEvent DUNGEONS_COG_CROSSBOW_SHOOT = registerSoundEvent("dungeons_cog_crossbow_shoot");
    public static final SoundEvent DUNGEONS_COG_CROSSBOW_PLACE = registerSoundEvent("dungeons_cog_crossbow_place");

    private static SoundEvent registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(CraftsmanBows.Mod_ID, name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    public static void init() {
        // コンソールにMODの初期化が完了したことを通知
        System.out.println("ModSoundEvents initialized");
    }
}

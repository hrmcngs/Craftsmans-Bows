package com.craftsman_bows.init;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import static com.craftsman_bows.CraftsmanBows.Mod_ID;

public class ModComponents {
    public static final Feature<Integer> BURST_COUNT = Registry.register(
            Registry.FEATURE,
            new ResourceLocation(Mod_ID, "burst_count"),
            new Feature<>(Codec.INT)
    );

    public static final Feature<Integer> BURST_STACK = Registry.register(
            Registry.FEATURE,
            new ResourceLocation(Mod_ID, "burst_stack"),
            new Feature<>(Codec.INT)
    );

    public static void init() {
        // コンソールにMODの初期化が完了したことを通知
        System.out.println("ModComponents initialized");
    }
}

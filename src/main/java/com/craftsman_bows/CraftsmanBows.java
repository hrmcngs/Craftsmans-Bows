package com.craftsman_bows;

import com.craftsman_bows.init.*;

public class CraftsmanBows {
    public static final String Mod_ID = "craftsman_bows";

    public static void onInitialize() {
        item.init();
        ModParticleTypes.init();
        ModSoundEvents.init();
        ModComponents.init();
    }
}

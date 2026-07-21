package com.craftsman_bows.client;

import com.craftsman_bows.CraftsmanBows;
import com.craftsman_bows.interfaces.item.ZoomItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Fabric 版の FovMixin（AbstractClientPlayerEntity#getFovMultiplier）に相当。
 * Forge には FOV 計算の公式イベントがあるのでそちらを使う。
 */
@Mod.EventBusSubscriber(modid = CraftsmanBows.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void computeFovModifier(ComputeFovModifierEvent event) {
        ItemStack itemStack = event.getPlayer().getUseItem();
        if (itemStack.getItem() instanceof ZoomItem zoomItem) {
            float fov = zoomItem.getFov();
            if (!Float.isNaN(fov)) {
                event.setNewFovModifier(fov);
                zoomItem.resetFov();
            }
        }
    }
}

package com.craftsman_bows;

import com.craftsman_bows.init.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 一時的な調査用。原因が判明したら削除する。 */
@Mod.EventBusSubscriber(modid = CraftsmanBows.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DebugProbe {

    private static final Logger LOG = LoggerFactory.getLogger("CB-PROBE");
    private static final boolean ENABLED = Boolean.getBoolean("craftsman_bows.probe");

    private static final int START = 100;
    private static final int END = 260;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!ENABLED || event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        int t = player.tickCount;
        String side = player.level().isClientSide ? "CLIENT" : "SERVER";

        if (t == START && !player.level().isClientSide) {
            player.setItemInHand(InteractionHand.MAIN_HAND,
                    new ItemStack(ModItems.REPEATER_CROSSBOW.get()));
            player.getInventory().add(new ItemStack(Items.ARROW, 64));
            player.getInventory().add(new ItemStack(Items.ARROW, 64));
            player.startUsingItem(InteractionHand.MAIN_HAND);
            LOG.info("[{}] t={} forced startUsingItem(repeater)", side, t);
        }

        if (t >= START && t <= END) {
            LOG.info("[{}] t={} using={} remaining={} item={} arrows={}",
                    side, t, player.isUsingItem(), player.getUseItemRemainingTicks(),
                    player.getMainHandItem().getItem(),
                    player.getInventory().countItem(Items.ARROW));
        }
    }
}

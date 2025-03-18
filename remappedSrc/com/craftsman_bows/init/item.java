package com.craftsman_bows.init;

import com.craftsman_bows.item.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.CreativeModeTabEvent.BuildContents;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.craftsman_bows.CraftsmanBows.Mod_ID;
import static net.minecraft.world.item.Items.NETHERITE_INGOT;

@Mod.EventBusSubscriber(modid = Mod_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    public static final Item SHORT_BOW = register("shortbow", new ShortBowItem(new Item.Properties().stacksTo(1).durability(576)));
    public static final Item LONG_BOW = register("longbow", new LongBowItem(new Item.Properties().stacksTo(1).durability(576)));
    public static final Item SHOT_CROSSBOW = register("shot_crossbow", new ShotCrossbowItem(new Item.Properties().stacksTo(1).durability(576)));
    public static final Item REPEATER_CROSSBOW = register("repeater_crossbow", new RepeaterCrossbowItem(new Item.Properties().stacksTo(1).durability(640).fireResistant()));
    public static final Item BURST_ARBALEST = register("burst_arbalest", new BurstArbalestItem(new Item.Properties().stacksTo(1).durability(900).fireResistant()));

    private static Item register(String id, Item item) {
        return Registry.register(Registry.ITEM, new ResourceLocation(Mod_ID, id), item);
    }

    @SubscribeEvent
    public static void onCreativeModeTabBuildContents(BuildContents event) {
        if (event.getTab() == CreativeModeTabs.TAB_COMBAT) {
            event.accept(new ItemStack(SHORT_BOW));
            event.accept(new ItemStack(LONG_BOW));
            event.accept(new ItemStack(SHOT_CROSSBOW));
            event.accept(new ItemStack(REPEATER_CROSSBOW));
            event.accept(new ItemStack(BURST_ARBALEST));
        }
    }
}

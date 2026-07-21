package com.craftsman_bows.init;

import com.craftsman_bows.item.BurstArbalestItem;
import com.craftsman_bows.item.LongBowItem;
import com.craftsman_bows.item.RepeaterCrossbowItem;
import com.craftsman_bows.item.ShortBowItem;
import com.craftsman_bows.item.ShotCrossbowItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Predicate;

import static com.craftsman_bows.CraftsmanBows.MOD_ID;

public class ModItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    /** 1.21 版の repairable(PLANKS) 相当 */
    private static final Predicate<ItemStack> PLANKS = stack -> stack.is(ItemTags.PLANKS);
    /** 1.21 版の repairable(NETHERITE_INGOT) 相当 */
    private static final Predicate<ItemStack> NETHERITE = stack -> stack.is(Items.NETHERITE_INGOT);

    public static final RegistryObject<Item> SHORT_BOW = ITEMS.register("shortbow",
            () -> new ShortBowItem(new Item.Properties().stacksTo(1).durability(576), PLANKS));

    public static final RegistryObject<Item> LONG_BOW = ITEMS.register("longbow",
            () -> new LongBowItem(new Item.Properties().stacksTo(1).durability(576), PLANKS));

    public static final RegistryObject<Item> SHOT_CROSSBOW = ITEMS.register("shot_crossbow",
            () -> new ShotCrossbowItem(new Item.Properties().stacksTo(1).durability(576), PLANKS));

    public static final RegistryObject<Item> REPEATER_CROSSBOW = ITEMS.register("repeater_crossbow",
            () -> new RepeaterCrossbowItem(new Item.Properties().stacksTo(1).durability(640).fireResistant(), NETHERITE));

    public static final RegistryObject<Item> BURST_ARBALEST = ITEMS.register("burst_arbalest",
            () -> new BurstArbalestItem(new Item.Properties().stacksTo(1).durability(900).fireResistant(), NETHERITE));

    public static final List<RegistryObject<Item>> ALL =
            List.of(SHORT_BOW, LONG_BOW, SHOT_CROSSBOW, REPEATER_CROSSBOW, BURST_ARBALEST);

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    /** アイテムのグループを武器に */
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            ALL.forEach(item -> event.accept(item.get()));
        }
    }
}

package com.craftsman_bows.init;

import com.craftsman_bows.enchantment.EndlessQuiverEnchantment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.craftsman_bows.CraftsmanBows.MOD_ID;

/**
 * この MOD が追加するエンチャントと、どのアイテムにどのエンチャントを付けられるかの指定。
 *
 * <p>付けられるかどうかはエンチャントの名前で判定する。1.21 版では
 * data/minecraft/tags/item/enchantable/{bow,durability}.json にアイテム名を並べて指定していたが、
 * 1.20.1 にそのタグは無く、バニラは EnchantmentCategory（BowItem を継承しているか、といった
 * 内部的な武器種別）で判定してしまう。ここではその内部判定を使わず、名前で明示的に許可リストを持つ。
 *
 * <p>アイテムごとに変えたい場合は {@code CraftsmanBowItem#allowedEnchantments()} を
 * サブクラスで override する。
 */
public final class ModEnchantments {

    // ------------------------------------------------------------------
    // 追加するエンチャント
    // ------------------------------------------------------------------

    /**
     * この MOD の遠距離武器だけが受け付けるカテゴリ。
     * バニラのカテゴリ（BOW など）にすると普通の弓にも付いてしまい、付けても何も起きないため。
     * 判定はアイテム名（名前空間）で行う。
     */
    public static final EnchantmentCategory CRAFTSMAN_BOWS_RANGED =
            EnchantmentCategory.create("CRAFTSMAN_BOWS_RANGED", ModEnchantments::isCraftsmanBowsItem);

    private static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MOD_ID);

    /** 無尽の矢筒: 効果付きの矢やスペクトルの矢も消費しなくなる */
    public static final RegistryObject<Enchantment> ENDLESS_QUIVER = ENCHANTMENTS.register("endless_quiver",
            () -> new EndlessQuiverEnchantment(Enchantment.Rarity.VERY_RARE,
                    CRAFTSMAN_BOWS_RANGED, EquipmentSlot.MAINHAND));

    // ------------------------------------------------------------------
    // 付けられるエンチャントの許可リスト（エンチャント名で指定する）
    // ------------------------------------------------------------------

    /** 弓だけに付くエンチャント（{@code #minecraft:enchantable/bow} のうち矢の性能を上げるもの） */
    public static final Set<ResourceLocation> BOW = names(
            "power",
            "punch",
            "flame");

    /** クロスボウだけに付くエンチャント（{@code EnchantmentCategory.CROSSBOW} 相当） */
    public static final Set<ResourceLocation> CROSSBOW = names(
            "quick_charge",
            "multishot",
            "piercing");

    /** 矢を無限にするエンチャント。弓にもクロスボウにも付く */
    public static final Set<ResourceLocation> INFINITY = names(
            "infinity",
            MOD_ID + ":endless_quiver");

    /** {@code #minecraft:enchantable/durability} 相当 */
    public static final Set<ResourceLocation> DURABILITY = names(
            "unbreaking",
            "mending");

    /** 弓タイプの武器（ショートボウ、ロングボウ）が受け付けるエンチャント */
    public static final Set<ResourceLocation> BOW_WEAPON = union(BOW, INFINITY, DURABILITY);

    /** クロスボウタイプの武器が受け付けるエンチャント。弓のエンチャントは付かない */
    public static final Set<ResourceLocation> CROSSBOW_WEAPON = union(CROSSBOW, INFINITY, DURABILITY);

    private ModEnchantments() {
    }

    public static void register(IEventBus modBus) {
        ENCHANTMENTS.register(modBus);
    }

    /** 許可リストに含まれるエンチャントかどうかを名前で判定する。 */
    public static boolean isAllowed(Set<ResourceLocation> allowed, Enchantment enchantment) {
        ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return id != null && allowed.contains(id);
    }

    private static boolean isCraftsmanBowsItem(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id != null && MOD_ID.equals(id.getNamespace());
    }

    /** 名前空間を省いた場合は minecraft: として扱う。 */
    private static Set<ResourceLocation> names(String... names) {
        return Arrays.stream(names)
                .map(ResourceLocation::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @SafeVarargs
    private static Set<ResourceLocation> union(Set<ResourceLocation>... sets) {
        Set<ResourceLocation> result = new LinkedHashSet<>();
        for (Set<ResourceLocation> set : sets) {
            result.addAll(set);
        }
        return Set.copyOf(result);
    }
}

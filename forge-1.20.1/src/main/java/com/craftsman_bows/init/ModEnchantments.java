package com.craftsman_bows.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * どのアイテムにどのエンチャントを付けられるかを、エンチャントの名前で指定する。
 *
 * <p>1.21 版では data/minecraft/tags/item/enchantable/{bow,durability}.json に
 * アイテム名を並べて指定していた。1.20.1 にそのタグは無く、バニラは EnchantmentCategory
 * （BowItem を継承しているか、といった内部的な武器種別）で判定してしまう。
 * ここではその内部判定を使わず、名前で明示的に許可リストを持つ。
 *
 * <p>アイテムごとに変えたい場合は {@code CraftsmanBowItem#allowedEnchantments()} を
 * サブクラスで override する。
 */
public final class ModEnchantments {

    /** {@code #minecraft:enchantable/bow} 相当 */
    public static final Set<ResourceLocation> BOW = names(
            "power",
            "punch",
            "flame",
            "infinity");

    /** {@code #minecraft:enchantable/durability} 相当 */
    public static final Set<ResourceLocation> DURABILITY = names(
            "unbreaking",
            "mending");

    /** この MOD の遠距離武器が既定で受け付けるエンチャント */
    public static final Set<ResourceLocation> RANGED_WEAPON = union(BOW, DURABILITY);

    private ModEnchantments() {
    }

    /** 許可リストに含まれるエンチャントかどうかを名前で判定する。 */
    public static boolean isAllowed(Set<ResourceLocation> allowed, Enchantment enchantment) {
        ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return id != null && allowed.contains(id);
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

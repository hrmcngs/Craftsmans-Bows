package com.craftsman_bows.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.MendingEnchantment;

/**
 * 無尽の矢筒。
 *
 * <p>バニラの「無限」は普通の矢しか無限にできない
 * （{@code ArrowItem#isInfinite} が {@code getClass() == ArrowItem.class} で弾いている）。
 * こちらは効果付きの矢やスペクトルの矢も消費しなくなる。
 *
 * <p>効果そのものは {@code CraftsmanBowItem#isInfinite} で見ている。
 */
public class EndlessQuiverEnchantment extends Enchantment {

    public EndlessQuiverEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
    }

    // コストはバニラの「無限」に合わせる
    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        // 「無限」とは効果が重なるので排他。修繕との排他はバニラの「無限」に倣った。
        return super.checkCompatibility(other)
                && other != Enchantments.INFINITY_ARROWS
                && !(other instanceof MendingEnchantment);
    }
}

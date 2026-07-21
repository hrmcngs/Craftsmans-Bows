package com.craftsman_bows.item;

import com.craftsman_bows.client.CraftsmanBowsClientExtensions;
import com.craftsman_bows.init.ModEnchantments;
import com.craftsman_bows.init.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * この MOD の遠距離武器の共通処理。
 * 1.21 の RangedWeaponItem#shootAll / load に相当する処理を 1.20.1 向けに書き起こしてある。
 */
public class CraftsmanBowItem extends BowItem {

    private final Predicate<ItemStack> repairIngredient;

    public CraftsmanBowItem(Properties properties, Predicate<ItemStack> repairIngredient) {
        super(properties);
        this.repairIngredient = repairIngredient;
    }

    // 1.21 版の enchantable(1) 相当
    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    /**
     * このアイテムに付けられるエンチャント。武器の種類ごとに変えたい場合はサブクラスで override する。
     */
    protected Set<ResourceLocation> allowedEnchantments() {
        return ModEnchantments.RANGED_WEAPON;
    }

    /**
     * 付けられるエンチャントをエンチャント名で判定する。
     *
     * <p>バニラは EnchantmentCategory（BowItem を継承しているか等の内部的な武器種別）で判定するが、
     * この MOD は見た目がクロスボウでも中身は弓、といったアイテムを持つのでそれでは指定できない。
     * Forge では {@code Enchantment#canEnchant} もこのフックに委譲されるため、
     * エンチャントテーブルでも金床でもここを通る。
     *
     * <p>ただし耐久力（unbreaking）だけは、バニラ側が「耐久値を持つアイテムなら無条件で可」と
     * 判定してしまうためここでは弾けない。この MOD ではどのみち許可しているので実害はない。
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return ModEnchantments.isAllowed(this.allowedEnchantments(), enchantment);
    }

    // 1.21 版の repairable(...) 相当
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return this.repairIngredient.test(repair);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CraftsmanBowsClientExtensions.INSTANCE);
    }

    // ------------------------------------------------------------------
    // 発射処理
    // ------------------------------------------------------------------

    /** 矢が無限（クリエイティブ、または無限系エンチャント）かどうか */
    protected static boolean isInfinite(Player player, ItemStack weapon, ItemStack ammo) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        // 無尽の矢筒は、バニラの「無限」が対象外にしている効果付きの矢なども消費しない
        if (weapon.getEnchantmentLevel(ModEnchantments.ENDLESS_QUIVER.get()) > 0) {
            return true;
        }
        return ammo.getItem() instanceof ArrowItem arrowItem && arrowItem.isInfinite(ammo, weapon, player);
    }

    /**
     * 矢のエンティティを生成し、発射方向・エンチャント効果まで適用して返す。
     * ワールドへの追加は呼び出し側で行う。
     */
    protected AbstractArrow makeArrow(Level level, Player player, ItemStack weapon, ItemStack ammo,
                                      float velocity, float inaccuracy, boolean critical) {
        ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;
        AbstractArrow arrow = this.customArrow(arrowItem.createArrow(level, ammo, player));
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, inaccuracy);

        if (critical) {
            arrow.setCritArrow(true);
        }

        int power = weapon.getEnchantmentLevel(Enchantments.POWER_ARROWS);
        if (power > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (double) power * 0.5 + 0.5);
        }

        int punch = weapon.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
        if (punch > 0) {
            arrow.setKnockback(punch);
        }

        if (weapon.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
            arrow.setSecondsOnFire(100);
        }

        if (isInfinite(player, weapon, ammo)) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
        return arrow;
    }

    /** 矢を 1 本発射し、武器の耐久を 1 減らす。 */
    protected void shootArrow(Level level, Player player, ItemStack weapon, ItemStack ammo,
                              float velocity, float inaccuracy, boolean critical) {
        AbstractArrow arrow = this.makeArrow(level, player, weapon, ammo, velocity, inaccuracy, critical);
        level.addFreshEntity(arrow);
        hurtWeapon(weapon, player, player.getUsedItemHand());
    }

    /** 矢をインベントリから 1 本消費する（無限なら消費しない）。 */
    protected static void consumeAmmo(Player player, ItemStack weapon, ItemStack ammo) {
        if (isInfinite(player, weapon, ammo)) {
            return;
        }
        ammo.shrink(1);
        if (ammo.isEmpty()) {
            player.getInventory().removeItem(ammo);
        }
    }

    protected static void hurtWeapon(ItemStack weapon, Player player, InteractionHand hand) {
        weapon.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
    }

    // ------------------------------------------------------------------
    // パーティクル
    // ------------------------------------------------------------------

    /** チャージ完了パーティクル */
    void chargeEndParticle(Level world, LivingEntity player) {
        spawnMuzzleParticle(world, player, ModParticleTypes.CHARGE_END.get());
    }

    /** 発射パーティクル */
    void shootParticle(Level world, LivingEntity player) {
        spawnMuzzleParticle(world, player, ModParticleTypes.SHOOT.get());
    }

    private void spawnMuzzleParticle(Level world, LivingEntity player, ParticleOptions particle) {
        // サーバー側では addParticle が何もしないので、座標計算ごと省く
        if (!world.isClientSide) {
            return;
        }

        // プレイヤーの視線方向を取得
        Vec3 lookDirection = player.getViewVector(1.0F);

        // オフセット
        double offsetRight;
        double offsetUp = -0.1; // 上に0.1ブロック分オフセット

        // 使用した手側にずらす
        if (player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            offsetRight = 0.3; // 右に0.3ブロック分オフセット
        } else {
            offsetRight = -0.3; // 左に0.3ブロック分オフセット
        }

        // 右方向のベクトルを取得する（視線ベクトルとY軸の外積）
        Vec3 horizontalDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 verticalDirection = horizontalDirection.cross(lookDirection).normalize();

        double distanceToTarget = 0.7; // プレイヤーから目標地点までの距離

        double particleX = player.getX() + lookDirection.x * distanceToTarget
                + horizontalDirection.x * offsetRight
                + verticalDirection.x * offsetUp;

        double particleY = player.getEyeY() + lookDirection.y * distanceToTarget
                + horizontalDirection.y * offsetRight
                + verticalDirection.y * offsetUp;

        double particleZ = player.getZ() + lookDirection.z * distanceToTarget
                + horizontalDirection.z * offsetRight
                + verticalDirection.z * offsetUp;

        // 視線の先にパーティクルを追加
        world.addParticle(particle, particleX, particleY, particleZ, 0, 0, 0);
    }

    /** チャージ中パーティクル */
    void chargingParticle(Level world, LivingEntity player) {
        // サーバー側では addParticle が何もしないので、座標計算ごと省く
        if (!world.isClientSide) {
            return;
        }

        // プレイヤーの視線方向を取得
        Vec3 lookDirection = player.getViewVector(1.0F);

        // 出現位置の範囲を設定
        double rangeX = 1.5;
        double rangeY = 1.5;
        double rangeZ = 1.5;

        // オフセット
        double offsetRight;
        double offsetUp = -0.1; // 上に0.1ブロック分オフセット

        // 使用した手側にずらす
        if (player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            offsetRight = 0.3;
        } else {
            offsetRight = -0.3;
        }

        // 右方向のベクトルを取得する（視線ベクトルとY軸の外積）
        Vec3 horizontalDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 verticalDirection = horizontalDirection.cross(lookDirection).normalize();

        // 目標位置（収束先）を設定し、オフセットを追加
        double distanceToTarget = 0.9;
        double targetX = player.getX() + lookDirection.x * distanceToTarget
                + horizontalDirection.x * offsetRight
                + verticalDirection.x * offsetUp;
        double targetY = player.getEyeY() + lookDirection.y * distanceToTarget
                + horizontalDirection.y * offsetRight
                + verticalDirection.y * offsetUp;
        double targetZ = player.getZ() + lookDirection.z * distanceToTarget
                + horizontalDirection.z * offsetRight
                + verticalDirection.z * offsetUp;

        // 視線方向に基づいた初期位置にランダムな偏差を加え、右方向にオフセット
        double particleX = player.getX() + lookDirection.x * 2.0
                + horizontalDirection.x * offsetRight
                + verticalDirection.x * offsetUp
                + (world.random.nextDouble() - 0.5) * rangeX;

        double particleY = player.getEyeY() + lookDirection.y * 2.0
                + horizontalDirection.y * offsetRight
                + verticalDirection.y * offsetUp
                + (world.random.nextDouble() - 0.5) * rangeY;

        double particleZ = player.getZ() + lookDirection.z * 2.0
                + horizontalDirection.z * offsetRight
                + verticalDirection.z * offsetUp
                + (world.random.nextDouble() - 0.5) * rangeZ;

        // パーティクルを追加し、収束先を設定
        world.addParticle(ModParticleTypes.CHARGE_DUST.get(), particleX, particleY, particleZ, targetX, targetY, targetZ);
    }
}

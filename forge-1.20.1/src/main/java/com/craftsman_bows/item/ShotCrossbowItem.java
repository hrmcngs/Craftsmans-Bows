package com.craftsman_bows.item;

import com.craftsman_bows.init.ModEnchantments;
import com.craftsman_bows.init.ModParticleTypes;
import com.craftsman_bows.init.ModSoundEvents;
import com.craftsman_bows.interfaces.entity.BypassCooldown;
import com.craftsman_bows.interfaces.item.CustomArmPoseItem;
import com.craftsman_bows.interfaces.item.CustomFirstPersonRender;
import com.craftsman_bows.interfaces.item.CustomUsingMoveItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.function.Predicate;

public class ShotCrossbowItem extends CraftsmanBowItem
        implements CustomUsingMoveItem, CustomArmPoseItem, CustomFirstPersonRender {

    public ShotCrossbowItem(Properties properties, Predicate<ItemStack> repairIngredient) {
        super(properties, repairIngredient);
    }

    // このアイテムはクロスボウ扱い。弓のエンチャント（power/punch/flame）は付かない
    @Override
    protected Set<ResourceLocation> allowedEnchantments() {
        return ModEnchantments.CROSSBOW_WEAPON;
    }

    /**
     * フルチャージ時に追加で撃たれる矢の本数。
     * 元は i == 20 のティックでフィールドに 4 を代入していたが、そのフィールドは
     * シングルプレイでクライアントと内部サーバーが共有してしまう上、
     * releaseUsing にはフルチャージ（i >= 20）でしか到達しないため定数で十分。
     */
    private static final int EXTRA_SHOTS = 4;

    // 移動速度はクライアントの操作にしか使わないので、書き込みはクライアント側だけに限定する
    float movementSpeed = 2.5f;

    // 最初の使用時のアクション
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        boolean bl = !user.getProjectile(itemStack).isEmpty();
        if (user.getAbilities().instabuild || bl) {
            user.startUsingItem(hand);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_LOAD.get(), 1.0f, 1.25f);
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    // アイテムを使用しているときの処理？
    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {

        if (world.isClientSide) {
            movementSpeed = 2.5f;
        }
        int i = this.getUseDuration(stack) - remainingUseTicks;

        // チャージ演出（パーティクルだけなのでクライアントでのみ計算する）
        if (world.isClientSide && !reached(stack, i, 20)) {
            // プレイヤーの視線方向を取得
            Vec3 lookDirection = user.getViewVector(1.0F);

            // 出現位置の範囲を設定
            double rangeX = 1.5;
            double rangeY = 1.5;
            double rangeZ = 1.5;

            // オフセット
            double offsetUp = -0.15; // 上に0.15ブロック分オフセット

            // ベクトルを取得
            Vec3 rightDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 verticalDirection = rightDirection.cross(lookDirection).normalize();

            // プレイヤーの視線先の位置を計算
            double distance = 2.0;

            // 目標位置（収束先）を設定
            double targetX = user.getX() + lookDirection.x + verticalDirection.x * offsetUp * distance;
            double targetY = user.getEyeY() + lookDirection.y + verticalDirection.y * offsetUp * distance;
            double targetZ = user.getZ() + lookDirection.z + verticalDirection.z * offsetUp * distance;

            double particleX = user.getX() + lookDirection.x * 2.0
                    + verticalDirection.x * offsetUp
                    + (world.random.nextDouble() - 0.5) * rangeX;

            double particleY = user.getEyeY() + lookDirection.y * 2.0
                    + verticalDirection.y * offsetUp
                    + (world.random.nextDouble() - 0.5) * rangeY;

            double particleZ = user.getZ() + lookDirection.z * 2.0
                    + verticalDirection.z * offsetUp
                    + (world.random.nextDouble() - 0.5) * rangeZ;

            // 視線の先にパーティクルを追加
            world.addParticle(ModParticleTypes.CHARGE_DUST.get(), particleX, particleY, particleZ, targetX, targetY, targetZ);
        }

        // 途中が寂しいので…
        if (reachedThisTick(stack, i, 10)) {
            user.playSound(SoundEvents.CROSSBOW_LOADING_MIDDLE, 1.0f, 1.0f);
        }

        if (reachedThisTick(stack, i, 20)) {
            user.playSound(SoundEvents.NOTE_BLOCK_XYLOPHONE.value(), 1.0f, 1.5f);
            user.playSound(SoundEvents.IRON_DOOR_CLOSE, 1.0f, 2f);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1.get(), 1.0f, 1.1f);

            // プレイヤーの視線方向を取得
            Vec3 lookDirection = user.getViewVector(1.0F);

            // オフセット
            double offsetUp = -0.15;

            // ベクトルを取得
            Vec3 rightDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 verticalDirection = rightDirection.cross(lookDirection).normalize();

            // プレイヤーの視線先の位置を計算
            double distance = 2.0;
            double particleX = user.getX() + lookDirection.x + verticalDirection.x * offsetUp * distance;
            double particleY = user.getEyeY() + lookDirection.y + verticalDirection.y * offsetUp * distance; // 目の高さ
            double particleZ = user.getZ() + lookDirection.z + verticalDirection.z * offsetUp * distance;

            // 視線の先にパーティクルを追加
            world.addParticle(ModParticleTypes.CHARGE_END.get(), particleX, particleY, particleZ, 0, 0, 0);
        }
    }

    // 矢の生成処理（無敵時間を貫通する）
    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        ((BypassCooldown) arrow).setBypassDamageCooldown();
        return arrow;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    /** 矢を 1 本発射する。pickup が false なら回収できない矢になる。 */
    protected void shootShotArrow(ServerLevel world, Player shooter, ItemStack stack, ItemStack ammo,
                                  float divergence, boolean pickup) {
        this.spreadArrows(world, shooter, stack, ammo, 1.2f, divergence, true, pickup);
        hurtWeapon(stack, shooter, shooter.getUsedItemHand());
    }

    // 使用をやめたとき、つまりクリックを離したときの処理だ。
    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player playerEntity)) {
            return;
        }

        // 手持ちの矢の種類を取得する
        ItemStack ammo = playerEntity.getProjectile(stack);
        if (ammo.isEmpty()) {
            return;
        }

        // フルチャージしていなければ発射しない（20 ティック分。クイックチャージで短くなる）
        int i = this.getUseDuration(stack) - remainingUseTicks;
        if (!reached(stack, i, 20)) {
            return;
        }

        // ここが放つ処理に見える。
        if (world instanceof ServerLevel serverWorld) {
            this.shootShotArrow(serverWorld, playerEntity, stack, ammo, 0.0f, true);
            for (int i2 = 0; i2 < EXTRA_SHOTS; i2++) {
                if (stack.isEmpty()) break;
                this.shootShotArrow(serverWorld, playerEntity, stack, ammo, 15.0f, false);
            }
        }

        consumeAmmo(playerEntity, stack, ammo);

        world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(),
                ModSoundEvents.LEGACY_BOW_SHOOT_2.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(),
                ModSoundEvents.DUNGEONS_BOW_SHOOT.get(), SoundSource.PLAYERS, 1.0f, 1.3f);
    }

    // インターフェース「CustomUsingMoveItem」として必要な処理
    @Override
    public float getMovementSpeed() {
        return movementSpeed;
    }

    @Override
    public void resetMovementSpeed() {
        movementSpeed = Float.NaN;
    }

    @Override
    public String getUsingFirstPersonRender() {
        return "CROSSBOW_HOLD";
    }

    @Override
    public String getStandbyFirstPersonRender() {
        return null;
    }

    // インターフェース「CustomArmPoseItem」として必要な処理
    @Override
    public String getUsingArmPose() {
        return "CROSSBOW_HOLD";
    }

    @Override
    public String getStandbyArmPose() {
        return "ITEM";
    }
}

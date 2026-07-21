package com.craftsman_bows.item;

import com.craftsman_bows.init.ModParticleTypes;
import com.craftsman_bows.init.ModSoundEvents;
import com.craftsman_bows.interfaces.entity.BypassCooldown;
import com.craftsman_bows.interfaces.item.CustomArmPoseItem;
import com.craftsman_bows.interfaces.item.CustomFirstPersonRender;
import com.craftsman_bows.interfaces.item.CustomUsingMoveItem;
import com.craftsman_bows.interfaces.item.ZoomItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class RepeaterCrossbowItem extends CraftsmanBowItem
        implements CustomArmPoseItem, CustomUsingMoveItem, CustomFirstPersonRender, ZoomItem {

    public RepeaterCrossbowItem(Properties properties, Predicate<ItemStack> repairIngredient) {
        super(properties, repairIngredient);
    }

    // 変数の定義。
    // どちらもクライアントの描画・操作にしか使わない。シングルプレイでは内部サーバーと
    // 同じ Item インスタンスを共有するので、書き込みはクライアント側だけに限定する。
    float movementSpeed = 5.0f;
    float fov;

    // 最初の使用時のアクション
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {

        // サウンド
        user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PICKUP.get(), 0.4f, 2.0f);
        user.playSound(SoundEvents.PISTON_CONTRACT, 1.0f, 1.5f);
        user.playSound(SoundEvents.IRON_DOOR_OPEN, 1.0f, 2f);

        // 変数リセット
        if (world.isClientSide) {
            movementSpeed = 3.0f;
            fov = Float.NaN;
        }

        // 腕振る処理
        InteractionHand activeHand = user.getUsedItemHand();
        if (activeHand == InteractionHand.MAIN_HAND) {
            user.swing(InteractionHand.OFF_HAND);
        } else if (activeHand == InteractionHand.OFF_HAND) {
            user.swing(InteractionHand.MAIN_HAND);
        }

        // 値を返す
        return ItemUtils.startUsingInstantly(world, user, hand);
    }

    // 右クリックを押し続けているときの処理
    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int useTick = this.getUseDuration(stack) - remainingUseTicks;

        if (world.isClientSide) {
            // 徐々に移動速度が下がっていく
            movementSpeed = 3.0f - (useTick * 0.1f);

            // 移動速度が負になると操作方向が逆になるので、0未満にならないようにする
            if (movementSpeed <= 0) {
                movementSpeed = 0.0f;
            }

            if (useTick >= 30) {
                fov = 0.8f;
            }
        }

        // チャージ演出（パーティクルだけなのでクライアントでのみ計算する）
        if (world.isClientSide && useTick <= 32) {
            // プレイヤーの視線方向を取得
            Vec3 lookDirection = user.getViewVector(1.0F);

            // 出現位置の範囲を設定
            double rangeX = 1.5;
            double rangeY = 1.5;
            double rangeZ = 1.5;

            // オフセット
            double offsetUp = -0.15;

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

        if (useTick == 15) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
        }
        if (useTick == 20) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.5f);
        }
        if (useTick == 30) {
            user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PICKUP.get(), 0.4f, 2.0f);
            user.playSound(SoundEvents.PISTON_EXTEND, 1.0f, 1.0f);
        }
        if (useTick == 31) {
            user.playSound(SoundEvents.PISTON_EXTEND, 1.0f, 1.5f);
        }
        if (useTick == 32) {
            user.playSound(SoundEvents.PISTON_EXTEND, 1.0f, 2.0f);
        }
        if (useTick == 40) {
            user.playSound(SoundEvents.IRON_DOOR_CLOSE, 1.0f, 2f);
            user.playSound(SoundEvents.NOTE_BLOCK_XYLOPHONE.value(), 1.0f, 1.5f);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_3.get(), 1.0f, 2.0f);

            Vec3 muzzle = muzzlePosition(user);

            double offsetX = (world.random.nextDouble() - 0.5) * 1;
            double offsetY = (world.random.nextDouble() - 0.5) * 1;
            double offsetZ = (world.random.nextDouble() - 0.5) * 1;

            // 視線の先にパーティクルを追加
            world.addParticle(ModParticleTypes.CHARGE_END.get(),
                    muzzle.x, muzzle.y, muzzle.z,
                    offsetX, offsetY, offsetZ);
        }
        // 完了して一拍置いてから射撃開始
        if (useTick >= 50) {
            this.gatlingShot(world, user, stack);
        }
        // あんまり長いこと撃ってると煙を吹き出す
        if (useTick == 82) {
            user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PICKUP.get(), 1.0f, 1.5f);
            user.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
        }
        if (world.isClientSide && useTick >= 82) {
            // もくもく警告パーティクル
            Vec3 muzzle = muzzlePosition(user);

            double offsetX = (world.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (world.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (world.random.nextDouble() - 0.5) * 0.3;

            world.addParticle(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, offsetX, offsetY, offsetZ);
        }
        // そろそろやばいぞ！
        if (useTick == 98) {
            user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PICKUP.get(), 1.0f, 1.5f);
            user.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
        }
        if (world.isClientSide && useTick >= 98) {
            // アチアチパーティクル
            Vec3 muzzle = muzzlePosition(user);

            double offsetX = (world.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (world.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (world.random.nextDouble() - 0.5) * 0.3;

            world.addParticle(ParticleTypes.LAVA, muzzle.x, muzzle.y, muzzle.z, offsetX, offsetY, offsetZ);
        }
        // それでも撃ち続けるとオーバーヒートする
        if (useTick == 113) {
            // サウンド
            user.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0f, 1.5f);
            user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PLACE.get(), 1.0f, 1f);

            // 吹っ飛ぶ
            float g = user.getYRot();
            float h = user.getXRot();
            float j = -Mth.sin(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
            float k = -Mth.sin(h * (float) (Math.PI / 180.0));
            float l = Mth.cos(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
            float m = Mth.sqrt(j * j + k * k + l * l);
            j *= (1 / m) * -1;
            k *= (1 / m) * -1;
            l *= (1 / m) * -1;
            user.push(j, k, l);

            Vec3 muzzle = muzzlePosition(user);

            // パーティクルを複数発生させるループ
            for (int i = 0; i < 10; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 0.3;
                double offsetY = (world.random.nextDouble() - 0.5) * 0.3;
                double offsetZ = (world.random.nextDouble() - 0.5) * 0.3;

                world.addParticle(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, offsetX, offsetY, offsetZ);

                // クールダウンに突入
                if (!(user instanceof Player playerEntity)) {
                    return;
                }
                playerEntity.getCooldowns().addCooldown(this, 60);
            }
        }
    }

    /** 銃口（視線の少し下）の座標 */
    private static Vec3 muzzlePosition(LivingEntity user) {
        Vec3 lookDirection = user.getViewVector(1.0F);
        double offsetUp = -0.15;
        Vec3 rightDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 verticalDirection = rightDirection.cross(lookDirection).normalize();
        double distance = 2.0;
        return new Vec3(
                user.getX() + lookDirection.x + verticalDirection.x * offsetUp * distance,
                user.getEyeY() + lookDirection.y + verticalDirection.y * offsetUp * distance, // 目の高さ
                user.getZ() + lookDirection.z + verticalDirection.z * offsetUp * distance);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    // 矢を発射する処理
    public void gatlingShot(Level world, LivingEntity user, ItemStack stack) {

        if (!(user instanceof Player playerEntity)) {
            return;
        }

        // 手持ちの矢の種類を取得する
        ItemStack ammo = playerEntity.getProjectile(stack);

        //　弾切れ時の処理
        if (ammo.isEmpty()) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
            return;
        }

        Vec3 muzzle = muzzlePosition(user);

        double offsetX = (world.random.nextDouble() - 0.5) * 1;
        double offsetY = (world.random.nextDouble() - 0.5) * 1;
        double offsetZ = (world.random.nextDouble() - 0.5) * 1;

        world.addParticle(ParticleTypes.CRIT, muzzle.x, muzzle.y, muzzle.z, offsetX, offsetY, offsetZ);

        // 後ろに下がっていく
        float g = playerEntity.getYRot();
        float h = playerEntity.getXRot();
        float j = -Mth.sin(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
        float k = -Mth.sin(h * (float) (Math.PI / 180.0));
        float l = Mth.cos(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
        float m = Mth.sqrt(j * j + k * k + l * l);
        j *= (float) (0.02 / m) * -1;
        k *= (float) (0.02 / m) * -1;
        l *= (float) (0.02 / m) * -1;
        playerEntity.push(j, k, l);

        // 音を鳴らす処理
        user.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.2f);

        // ワールドがサーバーなら？
        if (world instanceof ServerLevel serverWorld) {
            this.shootArrow(serverWorld, playerEntity, stack, ammo, 2.7f, 3.0f, false);
        }

        consumeAmmo(playerEntity, stack, ammo);
    }

    // 矢の生成処理（無敵時間を貫通する）
    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        ((BypassCooldown) arrow).setBypassDamageCooldown();
        return arrow;
    }

    // 使用をやめたとき、つまりクリックを離したときの処理だ。
    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player playerEntity)) {
            return;
        }
        if (world.isClientSide) {
            fov = Float.NaN;
        }

        // 使用時間に応じたクールタイムがかかる
        int useTick = this.getUseDuration(stack) - remainingUseTicks;

        if (useTick <= 82) {
            playerEntity.getCooldowns().addCooldown(this, 20);
        }

        if (useTick >= 82 && useTick <= 114) {
            playerEntity.getCooldowns().addCooldown(this, 30);
        }

        if (useTick == 114) {
            playerEntity.getCooldowns().addCooldown(this, 60);
        }

        user.playSound(SoundEvents.PISTON_CONTRACT, 1.0f, 1.5f);
        user.playSound(SoundEvents.IRON_DOOR_CLOSE, 1.0f, 2f);

        // 腕振る処理
        InteractionHand activeHand = user.getUsedItemHand();
        if (activeHand == InteractionHand.MAIN_HAND) {
            user.swing(InteractionHand.MAIN_HAND);
        } else if (activeHand == InteractionHand.OFF_HAND) {
            user.swing(InteractionHand.OFF_HAND);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 114;
    }

    // 1.21 版の hand_animation_on_swap: false 相当。
    // 連射中は毎ティック耐久値が変わるので、そのままだと構え直しモーションが出続けてしまう。
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    // インターフェースが欲しがってる処理
    @Override
    public String getStandbyArmPose() {
        return "ITEM";
    }

    @Override
    public String getUsingArmPose() {
        return "CROSSBOW_HOLD";
    }

    @Override
    public float getMovementSpeed() {
        return movementSpeed;
    }

    @Override
    public void resetMovementSpeed() {
    }

    @Override
    public String getUsingFirstPersonRender() {
        return "CROSSBOW_HOLD";
    }

    @Override
    public String getStandbyFirstPersonRender() {
        return null;
    }

    @Override
    public float getFov() {
        return this.fov;
    }

    @Override
    public void resetFov() {
        fov = Float.NaN;
    }
}

package com.craftsman_bows.item;

import com.craftsman_bows.init.ModParticleTypes;
import com.craftsman_bows.init.ModSoundEvents;
import com.craftsman_bows.interfaces.item.CustomArmPoseItem;
import com.craftsman_bows.interfaces.item.CustomFirstPersonRender;
import com.craftsman_bows.interfaces.item.CustomUsingMoveItem;
import com.craftsman_bows.interfaces.item.ZoomItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.List;

public class RepeaterCrossbowItem extends BowItem implements CustomArmPoseItem, CustomUsingMoveItem, CustomFirstPersonRender, ZoomItem {
    public RepeaterCrossbowItem(Item.Properties properties) {
        super(properties);
    }

    // 変数の定義
    float movementSpeed = 5.0f;
    float fov;

    // 最初の使用時のアクション
    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {

        // サウンド
        user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PICKUP, 0.4f, 2.0f);
        user.playSound(SoundEvents.PISTON_CONTRACT, 1.0f, 1.5f);
        user.playSound(SoundEvents.IRON_DOOR_OPEN, 1.0f, 2f);

        // 変数リセット
        movementSpeed = 3.0f;
        fov = Float.NaN;

        // 腕振る処理
        InteractionHand activeHand = user.getUsedItemHand();
        if (activeHand == InteractionHand.MAIN_HAND) {
            user.swing(InteractionHand.OFF_HAND);
        } else if (activeHand == InteractionHand.OFF_HAND) {
            user.swing(InteractionHand.MAIN_HAND);
        }

        // 値を返す
        return InteractionResult.CONSUME;
    }

    // 右クリックを押し続けているときの処理
    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int useTick = this.getUseDuration(stack) - remainingUseTicks;

        // 徐々に移動速度が下がっていく
        movementSpeed = 3.0f - (useTick * 0.1f);

        // 移動速度が負になると操作方向が逆になるので、0未満にならないようにする
        if (movementSpeed <= 0) {
            movementSpeed = 0.0f;
        }

        if (useTick >= 30) {
            fov = 0.8f;
        }

        // チャージ演出
        if (useTick <= 32) {
            // プレイヤーの視線方向を取得
            Vec3 lookDirection = user.getLookAngle();

            // 出現位置の範囲を設定
            double rangeX = 1.5;
            double rangeY = 1.5;
            double rangeZ = 1.5;

            // オフセット
            double offsetUp = -0.15; // 上に0.1ブロック分オフセット

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
                    + (level.random.nextDouble() - 0.5) * rangeX;

            double particleY = user.getEyeY() + lookDirection.y * 2.0
                    + verticalDirection.y * offsetUp
                    + (level.random.nextDouble() - 0.5) * rangeY;

            double particleZ = user.getZ() + lookDirection.z * 2.0
                    + verticalDirection.z * offsetUp
                    + (level.random.nextDouble() - 0.5) * rangeZ;

            // 視線の先にパーティクルを追加
            level.addParticle(ModParticleTypes.CHARGE_DUST, particleX, particleY, particleZ, targetX, targetY, targetZ);
        }

        if (useTick == 15) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
        }
        if (useTick == 20) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.5f);

        }
        if (useTick == 30) {
            user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PICKUP, 0.4f, 2.0f);
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
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_3, 1.0f, 2.0f);

            // プレイヤーの視線方向を取得
            Vec3 lookDirection = user.getLookAngle();

            // オフセット
            double offsetUp = -0.15; // 上に0.1ブロック分オフセット

            // ベクトルを取得
            Vec3 rightDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 verticalDirection = rightDirection.cross(lookDirection).normalize();

            // プレイヤーの視線先の位置を計算
            double distance = 2.0;
            double particleX = user.getX() + lookDirection.x + verticalDirection.x * offsetUp * distance;
            double particleY = user.getEyeY() + lookDirection.y + verticalDirection.y * offsetUp * distance;
            double particleZ = user.getZ() + lookDirection.z + verticalDirection.z * offsetUp * distance;

            // パーティクルを複数発生させるループ
            for (int i = 0; i < 1; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 1;
                double offsetY = (level.random.nextDouble() - 0.5) * 1;
                double offsetZ = (level.random.nextDouble() - 0.5) * 1;

                // 視線の先にパーティクルを追加
                level.addParticle(ModParticleTypes.CHARGE_END,
                        particleX, particleY, particleZ,
                        offsetX, offsetY, offsetZ);
            }
        }
        // 完了して一拍置いてから射撃開始
        if (useTick >= 50) {
            this.GatlingShot(level, user, stack);
        }
        // あんまり長いこと撃ってると煙を吹き出す
        if (useTick == 82) {
            user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PICKUP, 1.0f, 1.5f);
            user.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
        }
        if (useTick >= 82) {
            // もくもく警告パーティクル

            // プレイヤーの視線方向を取得
            Vec3 lookDirection = user.getLookAngle();

            // オフセット
            double offsetUp = -0.15; // 上に0.1ブロック分オフセット

            // ベクトルを取得
            Vec3 rightDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 verticalDirection = rightDirection.cross(lookDirection).normalize();

            // プレイヤーの視線先の位置を計算
            double distance = 2.0;
            double particleX = user.getX() + lookDirection.x + verticalDirection.x * offsetUp * distance;
            double particleY = user.getEyeY() + lookDirection.y + verticalDirection.y * offsetUp * distance; // 目の高さ
            double particleZ = user.getZ() + lookDirection.z + verticalDirection.z * offsetUp * distance;

            // パーティクルを複数発生させるループ
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

            // 視線の先にパーティクルを追加
            level.addParticle(ParticleTypes.SMOKE,
                    particleX, particleY, particleZ,
                    offsetX, offsetY, offsetZ);
        }
        // そろそろやばいぞ！
        if (useTick == 98) {
            user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PICKUP, 1.0f, 1.5f);
            user.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
        }
        if (useTick >= 98) {
            // アチアチパーティクル

            // プレイヤーの視線方向を取得
            Vec3 lookDirection = user.getLookAngle();

            // オフセット
            double offsetUp = -0.15; // 上に0.1ブロック分オフセット

            // ベクトルを取得
            Vec3 rightDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 verticalDirection = rightDirection.cross(lookDirection).normalize();

            // プレイヤーの視線先の位置を計算
            double distance = 2.0;
            double particleX = user.getX() + lookDirection.x + verticalDirection.x * offsetUp * distance;
            double particleY = user.getEyeY() + lookDirection.y + verticalDirection.y * offsetUp * distance; // 目の高さ
            double particleZ = user.getZ() + lookDirection.z + verticalDirection.z * offsetUp * distance;

            // パーティクルを複数発生させるループ
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

            // 視線の先にパーティクルを追加
            level.addParticle(ParticleTypes.LAVA,
                    particleX, particleY, particleZ,
                    offsetX, offsetY, offsetZ);
        }
        // それでも撃ち続けるとオーバーヒートする
        if (useTick == 113) {
            // サウンド
            user.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0f, 1.5f);
            user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_PLACE, 1.0f, 1f);

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

            // プレイヤーの視線方向を取得
            Vec3 lookDirection = user.getLookAngle();

            // オフセット
            double offsetUp = -0.15; // 上に0.1ブロック分オフセット

            // ベクトルを取得
            Vec3 rightDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 verticalDirection = rightDirection.cross(lookDirection).normalize();

            // プレイヤーの視線先の位置を計算
            double distance = 2.0;
            double particleX = user.getX() + lookDirection.x + verticalDirection.x * offsetUp * distance;
            double particleY = user.getEyeY() + lookDirection.y + verticalDirection.y * offsetUp * distance; // 目の高さ
            double particleZ = user.getZ() + lookDirection.z + verticalDirection.z * offsetUp * distance;

            // パーティクルを複数発生させるループ
            for (int i = 0; i < 10; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
                double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

                // 視線の先にパーティクルを追加
                level.addParticle(ParticleTypes.LARGE_SMOKE,
                        particleX, particleY, particleZ,
                        offsetX, offsetY, offsetZ);

                // クールダウンに突入
                if (!(user instanceof Player player)) {
                    return;
                }
                player.getCooldowns().addCooldown(stack.getItem(), 60);
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    // 矢を発射する処理
    public void GatlingShot(Level level, LivingEntity user, ItemStack stack) {

        // プレイヤーを定義する処理のようだ。後は…手持ちの矢の種類を取得する処理？
        Player player = (Player) user;
        ItemStack projectile = player.getProjectile(stack);

        // 弾切れ時の処理
        if (projectile.isEmpty()) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
            return;
        }

        // プレイヤーの視線方向を取得
        Vec3 lookDirection = user.getLookAngle();

        // オフセット
        double offsetUp = -0.15; // 上に0.1ブロック分オフセット

        // ベクトルを取得
        Vec3 rightDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 verticalDirection = rightDirection.cross(lookDirection).normalize();

        // プレイヤーの視線先の位置を計算
        double distance = 2.0;
        double particleX = user.getX() + lookDirection.x + verticalDirection.x * offsetUp * distance;
        double particleY = user.getEyeY() + lookDirection.y + verticalDirection.y * offsetUp * distance; // 目の高さ
        double particleZ = user.getZ() + lookDirection.z + verticalDirection.z * offsetUp * distance;

        // パーティクルを複数発生させるループ
        for (int i = 0; i < 1; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1;
            double offsetY = (level.random.nextDouble() - 0.5) * 1;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1;

            // 視線の先にパーティクルを追加
            level.addParticle(ParticleTypes.CRIT,
                    particleX, particleY, particleZ,
                    offsetX, offsetY, offsetZ);
        }

        // 後ろに下がっていく
        float g = player.getYRot();
        float h = player.getXRot();
        float j = -Mth.sin(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
        float k = -Mth.sin(h * (float) (Math.PI / 180.0));
        float l = Mth.cos(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
        float m = Mth.sqrt(j * j + k * k + l * l);
        j *= (float) (0.02 / m) * -1;
        k *= (float) (0.02 / m) * -1;
        l *=

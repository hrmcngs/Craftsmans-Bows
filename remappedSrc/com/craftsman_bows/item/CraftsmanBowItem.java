package com.craftsman_bows.item;

import com.craftsman_bows.init.ModParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;

public class CraftsmanBowItem extends BowItem {

    public CraftsmanBowItem(Item.Properties properties) {
        super(properties);
    }

    // チャージ完了パーティクル
    void chargeEndParticle(Level level, LivingEntity player) {
        // プレイヤーの視線方向を取得
        Vec3 lookDirection = player.getLookAngle();

        // オフセット
        double offsetRight;
        double offsetUp = -0.1; // 上に0.1ブロック分オフセット

        // 使用した手側にずらす
        InteractionHand activeHand = player.getUsedItemHand();
        if (activeHand == InteractionHand.MAIN_HAND) {
            offsetRight = 0.3; // 右に0.3ブロック分オフセット
        } else {
            offsetRight = -0.3; // 左にに0.3ブロック分オフセット
        }

        // 右方向のベクトルを取得する（視線ベクトルとY軸の外積）
        Vec3 horizontalDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 verticalDirection = horizontalDirection.cross(lookDirection).normalize();

        // 複数のパーティクルを発生させるループ
        for (int i = 0; i < 1; i++) {
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

            double offsetX = 0;
            double offsetY = 0;
            double offsetZ = 0;

            // 視線の先にパーティクルを追加
            level.addParticle((SimpleParticleType) ModParticleTypes.CHARGE_END,
                    particleX, particleY, particleZ,
                    offsetX, offsetY, offsetZ);
        }
    }

    // 発射パーティクル
    void shootParticle(Level level, LivingEntity player) {
        // プレイヤーの視線方向を取得
        Vec3 lookDirection = player.getLookAngle();

        // オフセット
        double offsetRight;
        double offsetUp = -0.1; // 上に0.1ブロック分オフセット

        // 使用した手側にずらす
        InteractionHand activeHand = player.getUsedItemHand();
        if (activeHand == InteractionHand.MAIN_HAND) {
            offsetRight = 0.3; // 右に0.3ブロック分オフセット
        } else {
            offsetRight = -0.3; // 左にに0.3ブロック分オフセット
        }

        // 右方向のベクトルを取得する（視線ベクトルとY軸の外積）
        Vec3 horizontalDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 verticalDirection = horizontalDirection.cross(lookDirection).normalize();

        // 複数のパーティクルを発生させるループ
        for (int i = 0; i < 1; i++) {
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

            double offsetX = 0;
            double offsetY = 0;
            double offsetZ = 0;

            // 視線の先にパーティクルを追加
            level.addParticle((SimpleParticleType) ModParticleTypes.SHOOT,
                    particleX, particleY, particleZ,
                    offsetX, offsetY, offsetZ);
        }
    }

    // 発射パーティクル
    void chargingParticle(Level level, LivingEntity player) {
        // プレイヤーの視線方向を取得
        Vec3 lookDirection = player.getLookAngle();

        // 出現位置の範囲を設定 (ここではX, Y, Zにそれぞれ0.3の範囲でばらつきを持たせます)
        double rangeX = 1.5;
        double rangeY = 1.5;
        double rangeZ = 1.5;

        // オフセット
        double offsetRight;
        double offsetUp = -0.1; // 上に0.1ブロック分オフセット

        // 使用した手側にずらす
        InteractionHand activeHand = player.getUsedItemHand();
        if (activeHand == InteractionHand.MAIN_HAND) {
            offsetRight = 0.3; // 右に0.3ブロック分オフセット
        } else {
            offsetRight = -0.3; // 左にに0.3ブロック分オフセット
        }

        // 右方向のベクトルを取得する（視線ベクトルとY軸の外積）
        Vec3 horizontalDirection = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 verticalDirection = horizontalDirection.cross(lookDirection).normalize();

        // 目標位置（収束先）を設定し、オフセットを追加
        double distanceToTarget = 0.9; // プレイヤーから目標地点までの距離
        double targetX = player.getX() + lookDirection.x * distanceToTarget
                + horizontalDirection.x * offsetRight
                + verticalDirection.x * offsetUp;
        double targetY = player.getEyeY() + lookDirection.y * distanceToTarget
                + horizontalDirection.y * offsetRight
                + verticalDirection.y * offsetUp;
        double targetZ = player.getZ() + lookDirection.z * distanceToTarget
                + horizontalDirection.z * offsetRight
                + verticalDirection.z * offsetUp;

        // 複数のパーティクルを発生させるループ
        for (int i = 0; i < 1; i++) {
            // 視線方向に基づいた初期位置にランダムな偏差を加え、右方向にオフセット
            double particleX = player.getX() + lookDirection.x * 2.0
                    + horizontalDirection.x * offsetRight
                    + verticalDirection.x * offsetUp
                    + (level.random.nextDouble() - 0.5) * rangeX;

            double particleY = player.getEyeY() + lookDirection.y * 2.0
                    + horizontalDirection.y * offsetRight
                    + verticalDirection.y * offsetUp
                    + (level.random.nextDouble() - 0.5) * rangeY;

            double particleZ = player.getZ() + lookDirection.z * 2.0
                    + horizontalDirection.z * offsetRight
                    + verticalDirection.z * offsetUp
                    + (level.random.nextDouble() - 0.5) * rangeZ;

            // パーティクルを追加し、収束先を設定
            level.addParticle((SimpleParticleType) ModParticleTypes.CHARGE_DUST, particleX, particleY, particleZ, targetX, targetY, targetZ);
        }
    }
}

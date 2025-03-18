package com.craftsman_bows.item;

import com.craftsman_bows.init.ModSoundEvents;
import com.craftsman_bows.interfaces.item.CustomUsingMoveItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.craftsman_bows.init.ModComponents;

import java.util.List;

public class BurstArbalestItem extends CraftsmanBowItem implements CustomUsingMoveItem {
    public BurstArbalestItem(Item.Properties properties) {
        super(properties);
    }

    // 最初の使用時のアクション
    @Override
    public ActionResult<ItemStack> use(Level level, Player player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // サウンド
        player.playSound(ModSoundEvents.DUNGEONS_BOW_LOAD, 1.0f, 1.1f);
        player.playSound(SoundEvents.IRON_DOOR_OPEN, 1.0f, 2f);

        // 腕振る処理
        Hand activeHand = player.getUsedItemHand();
        if (activeHand == Hand.MAIN_HAND) {
            player.swing(Hand.OFF_HAND);
        } else if (activeHand == Hand.OFF_HAND) {
            player.swing(Hand.MAIN_HAND);
        }

        stack.getOrCreateTag().putInt("BurstStack", 0);
        stack.getOrCreateTag().putInt("BurstCount", 0);

        // 値を返す
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    // 右クリックを押し続けているときの処理
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        int useTick = getUseDuration(stack) - count;

        // クライアント、サーバーともに行う処理
        if (useTick < 70) {
            chargingParticle(level, entity);
        }

        // チャージ段階ごと
        if (useTick == 20 || useTick == 45 || useTick == 70) {
            chargeEndParticle(level, entity);
        }

        // チャージ1
        if (useTick == 10) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.1f);
            entity.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1, 1.0f, 1.0f);
        }
        if (useTick == 15) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.2f);
        }
        if (useTick == 20) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.3f);
            entity.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_3, 1.0f, 1.0f);
        }

        // チャージ2
        if (useTick == 35) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.4f);
            entity.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1, 1.0f, 1.25f);
        }
        if (useTick == 40) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.5f);
        }
        if (useTick == 45) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.6f);
            entity.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_3, 1.0f, 1.5f);
        }

        // チャージ3
        if (useTick == 60) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.7f);
            entity.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1, 1.0f, 1.5f);
        }
        if (useTick == 65) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.8f);
        }
        if (useTick == 70) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 2.0f);
            entity.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_3, 1.0f, 2.0f);
        }

        // サーバーのみ
        if (level instanceof ServerLevel) {
            // チャージカウントが進む
            if (useTick == 10 || useTick == 15 || useTick == 20 || useTick == 35 || useTick == 40 || useTick == 45 || useTick == 60 || useTick == 65 || useTick == 70) {
                int countValue = stack.getOrCreateTag().getInt("BurstStack");
                stack.getOrCreateTag().putInt("BurstStack", ++countValue);
            }
        }
    }

    // 矢を発射する処理
    public void burstShot(Level level, LivingEntity entity, ItemStack stack) {
        // プレイヤーを定義する処理のようだ。後は…手持ちの矢の種類を取得する処理？
        Player player = (Player) entity;
        ItemStack projectile = player.getProjectile(stack);

        // 弾切れ時の処理
        if (projectile.isEmpty()) {
            entity.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
            return;
        }

        // 弓につがえた矢を取得している？
        List<ItemStack> list = BowItem.getChargedProjectiles(stack);

        // 音を鳴らす処理
        entity.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.2f);

        // 後ろに下がっていく
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        float x = -Mth.sin(yaw * (float) (Math.PI / 180.0)) * Mth.cos(pitch * (float) (Math.PI / 180.0));
        float y = -Mth.sin(pitch * (float) (Math.PI / 180.0));
        float z = Mth.cos(yaw * (float) (Math.PI / 180.0)) * Mth.cos(pitch * (float) (Math.PI / 180.0));
        float length = Mth.sqrt(x * x + y * y + z * z);
        x *= (float) (0.02 / length) * -1;
        y *= (float) (0.02 / length) * -1;
        z *= (float) (0.02 / length) * -1;
        player.push(x, y, z);

        // プレイヤーの視線方向を取得
        Vec3 lookDirection = entity.getLookAngle();
        double distance = 1.0;
        double particleX = entity.getX() + lookDirection.x * distance;
        double particleY = entity.getEyeY() + lookDirection.y * distance;
        double particleZ = entity.getZ() + lookDirection.z * distance;

        // パーティクルを複数発生させるループ
        double offsetX = (level.random.nextDouble() - 0.5) * 1;
        double offsetY = (level.random.nextDouble() - 0.5) * 1;
        double offsetZ = (level.random.nextDouble() - 0.5) * 1;

        // 視線の先にパーティクルを追加
        level.addParticle(ParticleTypes.CRIT, particleX, particleY, particleZ, offsetX, offsetY, offsetZ);

        // ワールドがサーバーなら
        if (level instanceof ServerLevel serverLevel) {
            if (!list.isEmpty()) {
                this.shootAll(serverLevel, player, player.getUsedItemHand(), stack, list, 2.7f, 1.0f, false, null);
            }
        }
    }

    // 持ってる間の処理…？
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (entity instanceof LivingEntity user) {
            if (selected || user.getOffhandItem() == stack) {
                int burstCount = stack.getOrCreateTag().getInt("BurstCount");

                if (burstCount >= 1) {
                    burstShot(level, user, stack);
                    stack.getOrCreateTag().putInt("BurstCount", burstCount - 1);

                    if (user instanceof Player player && burstCount == 1) {
                        user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_SHOOT, 1.0f, 0.8f);
                        user.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
                        player.getCooldowns().addCooldown(stack.getItem(), 15);

                        // プレイヤーの視線方向を取得
                        Vec3 lookDirection = user.getLookAngle();
                        double distance = 1.0;
                        double particleX = user.getX() + lookDirection.x * distance;
                        double particleY = user.getEyeY() + lookDirection.y * distance;
                        double particleZ = user.getZ() + lookDirection.z * distance;

                        // パーティクルを複数発生させるループ
                        for (int i = 0; i < 10; i++) {
                            double offsetX = (level.random.nextDouble() - 0.5) * 0.2;
                            double offsetY = (level.random.nextDouble() - 0.5) * 0.2;
                            double offsetZ = (level.random.nextDouble() - 0.5) * 0.2;

                            // 視線の先にパーティクルを追加
                            level.addParticle(ParticleTypes.SMOKE, particleX, particleY, particleZ, offsetX, offsetY, offsetZ);
                        }
                    }
                }
            }
        }
    }

    // 矢の生成処理
    @Override
    protected Projectile createArrow(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        Item item = projectileStack.getItem();
        ArrowItem arrowItem = item instanceof ArrowItem ? (ArrowItem) item : (ArrowItem) Items.ARROW;
        AbstractArrow arrow = arrowItem.createArrow(level, projectileStack, shooter);
        arrow.setNoGravity(true);
        return arrow;
    }

    // 使用をやめたとき、つまりクリックを離したときの処理だ。
    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        int burstStack = stack.getOrCreateTag().getInt("BurstStack");
        stack.getOrCreateTag().putInt("BurstCount", burstStack);
        stack.getOrCreateTag().remove("BurstStack");

        // バースト数に応じたクールタイムを設定
        if (burstStack >= 1) {
            player.getCooldowns().addCooldown(stack.getItem(), 150);
        } else {
            player.getCooldowns().addCooldown(stack.getItem(), 15);
        }

        entity.playSound(SoundEvents.PISTON_CONTRACT, 1.0f, 1.5f);
        entity.playSound(SoundEvents.IRON_DOOR_CLOSE, 1.0f, 2f);

        // 腕振る処理
        Hand activeHand = entity.getUsedItemHand();
        if (activeHand == Hand.MAIN_HAND) {
            entity.swing(Hand.MAIN_HAND);
        } else if (activeHand == Hand.OFF_HAND) {
            entity.swing(Hand.OFF_HAND);
        }

        return true;
    }

    @Override
    public float getMovementSpeed() {
        return 2.5f;
    }

    @Override
    public void resetMovementSpeed() {
    }
}

package com.craftsman_bows.item;

import com.craftsman_bows.init.ModSoundEvents;
import com.craftsman_bows.interfaces.entity.BypassCooldown;
import com.craftsman_bows.interfaces.item.CustomUsingMoveItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class BurstArbalestItem extends CraftsmanBowItem implements CustomUsingMoveItem {

    // 1.21 版のデータコンポーネント（craftsman_bows:burst_count / burst_stack）に相当する NBT キー
    private static final String BURST_COUNT = "BurstCount";
    private static final String BURST_STACK = "BurstStack";

    public BurstArbalestItem(Properties properties, Predicate<ItemStack> repairIngredient) {
        super(properties, repairIngredient);
    }

    private static int getCounter(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(key);
    }

    private static void setCounter(ItemStack stack, String key, int value) {
        stack.getOrCreateTag().putInt(key, value);
    }

    // 最初の使用時のアクション
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {

        ItemStack stack = user.getItemInHand(hand);

        // サウンド
        user.playSound(ModSoundEvents.DUNGEONS_BOW_LOAD.get(), 1.0f, 1.1f);
        user.playSound(SoundEvents.IRON_DOOR_OPEN, 1.0f, 2f);

        // 腕振る処理
        InteractionHand activeHand = user.getUsedItemHand();
        if (activeHand == InteractionHand.MAIN_HAND) {
            user.swing(InteractionHand.OFF_HAND);
        } else if (activeHand == InteractionHand.OFF_HAND) {
            user.swing(InteractionHand.MAIN_HAND);
        }

        setCounter(stack, BURST_STACK, 0);
        setCounter(stack, BURST_COUNT, 0);

        // 値を返す
        return ItemUtils.startUsingInstantly(world, user, hand);
    }

    // 右クリックを押し続けているときの処理
    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int useTick = this.getUseDuration(stack) - remainingUseTicks;

        // クライアント、サーバーともに行う処理
        if (useTick < 70) {
            chargingParticle(world, user);
        }

        // チャージ段階ごと
        if (useTick == 20 || useTick == 45 || useTick == 70) {
            chargeEndParticle(world, user);
        }

        // チャージ1
        if (useTick == 10) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.1f);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1.get(), 1.0f, 1.0f);
        }
        if (useTick == 15) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.2f);
        }
        if (useTick == 20) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.3f);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_3.get(), 1.0f, 1.0f);
        }

        // チャージ2
        if (useTick == 35) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.4f);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1.get(), 1.0f, 1.25f);
        }
        if (useTick == 40) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.5f);
        }
        if (useTick == 45) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.6f);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_3.get(), 1.0f, 1.5f);
        }

        // チャージ3
        if (useTick == 60) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.7f);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1.get(), 1.0f, 1.5f);
        }
        if (useTick == 65) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 1.8f);
        }
        if (useTick == 70) {
            user.playSound(SoundEvents.STONE_BUTTON_CLICK_ON, 1.0f, 2.0f);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_3.get(), 1.0f, 2.0f);
        }

        // サーバーのみ
        if (world instanceof ServerLevel) {
            // チャージカウントが進む
            if (useTick == 10 || useTick == 15 || useTick == 20
                    || useTick == 35 || useTick == 40 || useTick == 45
                    || useTick == 60 || useTick == 65 || useTick == 70) {
                setCounter(stack, BURST_STACK, getCounter(stack, BURST_STACK) + 1);
            }
        }
    }

    // 矢を発射する処理
    public void burstShot(Level world, LivingEntity user, ItemStack stack) {

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

        // 音を鳴らす処理
        user.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.2f);

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

        // プレイヤーの視線先の位置を計算（プレイヤーの位置から1ブロック先にパーティクルを表示）
        Vec3 lookDirection = user.getViewVector(1.0F);
        double distance = 1.0;
        double particleX = user.getX() + lookDirection.x * distance;
        double particleY = user.getEyeY() + lookDirection.y * distance; // 目の高さ
        double particleZ = user.getZ() + lookDirection.z * distance;

        double offsetX = (world.random.nextDouble() - 0.5) * 1;
        double offsetY = (world.random.nextDouble() - 0.5) * 1;
        double offsetZ = (world.random.nextDouble() - 0.5) * 1;

        // 視線の先にパーティクルを追加
        world.addParticle(ParticleTypes.CRIT, particleX, particleY, particleZ, offsetX, offsetY, offsetZ);

        // ワールドがサーバーなら
        if (world instanceof ServerLevel serverWorld) {
            this.shootArrow(serverWorld, playerEntity, stack, ammo, 2.7f, 1.0f, false);
        }

        consumeAmmo(playerEntity, stack, ammo);
    }

    // 持ってる間の処理…？
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (entity instanceof LivingEntity user) {
            if (selected || user.getOffhandItem() == stack) {

                int burstCount = getCounter(stack, BURST_COUNT);

                if (burstCount >= 1) {
                    burstShot(world, user, stack);

                    // 更新した値を保存
                    setCounter(stack, BURST_COUNT, burstCount - 1);

                    if ((user instanceof Player playerEntity) && burstCount == 1) {
                        user.playSound(ModSoundEvents.DUNGEONS_COG_CROSSBOW_SHOOT.get(), 1.0f, 0.8f);
                        user.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
                        playerEntity.getCooldowns().addCooldown(this, 15);

                        // プレイヤーの視線先の位置を計算
                        Vec3 lookDirection = user.getViewVector(1.0F);
                        double distance = 1.0;
                        double particleX = user.getX() + lookDirection.x * distance;
                        double particleY = user.getEyeY() + lookDirection.y * distance; // 目の高さ
                        double particleZ = user.getZ() + lookDirection.z * distance;

                        // パーティクルを複数発生させるループ
                        for (int i = 0; i < 10; i++) {
                            double offsetX = (world.random.nextDouble() - 0.5) * 0.2;
                            double offsetY = (world.random.nextDouble() - 0.5) * 0.2;
                            double offsetZ = (world.random.nextDouble() - 0.5) * 0.2;

                            world.addParticle(ParticleTypes.SMOKE, particleX, particleY, particleZ,
                                    offsetX, offsetY, offsetZ);
                        }
                    }
                }
            }
        }
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

        int burstStack = getCounter(stack, BURST_STACK);
        setCounter(stack, BURST_COUNT, burstStack);
        if (stack.getTag() != null) {
            stack.getTag().remove(BURST_STACK);
        }

        // バースト数に応じたクールタイムを設定
        if (burstStack >= 1) {
            playerEntity.getCooldowns().addCooldown(this, 150);
        } else {
            playerEntity.getCooldowns().addCooldown(this, 15);
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

    // 1.21 版の hand_animation_on_swap: false 相当。
    // 連射中は毎ティック NBT が変わるので、そのままだと構え直しモーションが出続けてしまう。
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public float getMovementSpeed() {
        return 2.5f;
    }

    @Override
    public void resetMovementSpeed() {
    }
}

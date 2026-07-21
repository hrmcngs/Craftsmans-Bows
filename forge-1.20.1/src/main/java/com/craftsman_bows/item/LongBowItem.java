package com.craftsman_bows.item;

import com.craftsman_bows.init.ModSoundEvents;
import com.craftsman_bows.interfaces.item.ZoomItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public class LongBowItem extends CraftsmanBowItem implements ZoomItem {

    public LongBowItem(Properties properties, Predicate<ItemStack> repairIngredient) {
        super(properties, repairIngredient);
    }

    float fov;

    // 弓を引いた時間を取得する処理
    public static float getPullProgress(int useTicks) {
        float f = (float) useTicks / 30.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    // 最初の使用時のアクション
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        boolean bl = !user.getProjectile(itemStack).isEmpty();
        if (!user.getAbilities().instabuild && !bl) {
            return InteractionResultHolder.fail(itemStack);
        } else {
            user.startUsingItem(hand);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_LOAD.get(), 1.0f, 1.0f);
            if (world.isClientSide) {
                fov = 1f;
            }
            return InteractionResultHolder.consume(itemStack);
        }
    }

    // アイテムを使用しているときの処理
    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int i = this.getUseDuration(stack) - remainingUseTicks;

        // 途中が寂しいので…
        if (i == 10) {
            user.playSound(SoundEvents.CROSSBOW_LOADING_MIDDLE, 1.0f, 1.2f);
        }

        // チャージ中
        if (i < 29) {
            chargingParticle(world, user);  // パーティクル生成の処理
        }

        // チャージ完了
        if (i == 29) {
            chargeEndParticle(world, user);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1.get(), 1.0f, 1.0f);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_4.get(), 1.0f, 1.2f);
        }

        // ズーム処理（fov はクライアントの描画にしか使わないので、クライアント側でだけ更新する。
        // 両サイドで書き込むと、シングルプレイでは内部サーバーのスレッドと値を奪い合って画面が揺れる）
        if (world.isClientSide) {
            fov = 1.0f - getPullProgress(i) / 3f;
        }
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

        // 手持ちの矢の種類を取得する
        ItemStack ammo = playerEntity.getProjectile(stack);
        if (ammo.isEmpty()) {
            return;
        }

        // 使用時間0.1未満では使用をキャンセルする処理のようだ
        int i = this.getUseDuration(stack) - remainingUseTicks;
        float f = getPullProgress(i);
        if ((double) f < 0.1) {
            return;
        }

        // パーティクル
        if (f >= 1) {
            shootParticle(world, user);
        }

        // ここが放つ処理に見える。
        if (world instanceof ServerLevel serverWorld) {
            if (f >= 1) {
                this.shootArrow(serverWorld, playerEntity, stack, ammo, f * 4.0f, 0.0f, true);
            } else {
                this.shootArrow(serverWorld, playerEntity, stack, ammo, f * 2.0f, 0.0f, f == 0.0f);
            }

            if (f < 1) {
                world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(),
                        SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);
            } else {
                world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(),
                        ModSoundEvents.LEGACY_BOW_SHOOT_1.get(), SoundSource.PLAYERS, 1.0f, 1.3f);
                world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(),
                        ModSoundEvents.DUNGEONS_BOW_SHOOT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        consumeAmmo(playerEntity, stack, ammo);
    }

    // インターフェースとして持っておくべきやつ
    @Override
    public void resetFov() {
        fov = Float.NaN;
    }

    @Override
    public float getFov() {
        return this.fov;
    }
}

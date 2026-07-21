package com.craftsman_bows.item;

import com.craftsman_bows.init.ModSoundEvents;
import com.craftsman_bows.interfaces.item.CanSprintWhileUsing;
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

public class ShortBowItem extends CraftsmanBowItem implements CanSprintWhileUsing {

    public ShortBowItem(Properties properties, Predicate<ItemStack> repairIngredient) {
        super(properties, repairIngredient);
    }

    // 弓を引いた時間を取得する処理のようだ。今回は書き換えて、0.55以上引き絞ったら強制的に1（フルチャージ）になるようにした
    public static float getPullProgress(int useTicks) {
        float f = (float) useTicks / 20.0f;
        if ((f = (f * f + f * 2.0f) / 3.0f) > 0.55f) {
            f = 1f;
        }
        return f;
    }

    // アイテムを使用しているときの処理
    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int i = this.getUseDuration(stack) - remainingUseTicks;

        // チャージ中
        if (i < 10) {
            chargingParticle(world, user);
        }

        // チャージ完了
        if (i == 10) {
            chargeEndParticle(world, user);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1.get(), 1.0f, 1.4f);
        }
    }

    // 最初の使用時のアクション
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        boolean bl = !user.getProjectile(itemStack).isEmpty();
        if (!user.getAbilities().instabuild && !bl) {
            return InteractionResultHolder.fail(itemStack);
        } else {
            user.playSound(ModSoundEvents.DUNGEONS_BOW_LOAD.get(), 1.0f, 1.2f);
            user.startUsingItem(hand);
            return InteractionResultHolder.consume(itemStack);
        }
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
            this.shootArrow(serverWorld, playerEntity, stack, ammo, f * 1.6f, 1.0f, f == 1.0f);

            if (f < 1) {
                world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(),
                        SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f,
                        1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + f * 0.5f);
            } else {
                world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(),
                        ModSoundEvents.LEGACY_BOW_SHOOT_1.get(), SoundSource.PLAYERS, 1.0f,
                        0.8f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + 0.9f);
                world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(),
                        ModSoundEvents.DUNGEONS_BOW_SHOOT.get(), SoundSource.PLAYERS, 1.0f, 1.4f);
            }
        }

        consumeAmmo(playerEntity, stack, ammo);
    }
}

package com.craftsman_bows.item;

import com.craftsman_bows.interfaces.item.CanSprintWhileUsing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import com.craftsman_bows.init.ModSoundEvents;

import java.util.List;

public class LongBowItem extends CraftsmanBowItem implements CanSprintWhileUsing {
    public LongBowItem(Item.Properties properties) {
        super(properties);
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
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int i = this.getUseDuration(stack) - remainingUseTicks;

        // チャージ中
        if (i < 10) {
            chargingParticle(level, user);
        }

        // チャージ完了
        if (i == 10) {
            chargeEndParticle(level, user);
            user.playSound(ModSoundEvents.DUNGEONS_BOW_CHARGE_1, 1.0f, 1.4f);
        }
    }

    // 最初の使用時のアクション
    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        boolean bl = !user.getProjectile(itemStack).isEmpty();
        if (!user.getAbilities().instabuild && !bl) {
            return InteractionResult.FAIL;
        } else {
            user.playSound(ModSoundEvents.DUNGEONS_BOW_LOAD, 1.0f, 1.2f);
            user.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }

    // 使用をやめたとき、つまりクリックを離したときの処理だ。
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            return;
        }

        // プレイヤーを定義する処理のようだ。後は…手持ちの矢の種類を取得する処理？
        ItemStack itemStack = player.getProjectile(stack);
        if (itemStack.isEmpty()) {
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
            shootParticle(level, user);
        }

        // ここが放つ処理に見える。
        List<ItemStack> list = BowItem.getChargedProjectiles(stack);
        if (level instanceof ServerLevel serverLevel) {
            if (!list.isEmpty()) {
                this.performShooting(serverLevel, player, player.getUsedItemHand(), stack, list, f * 1.6f, 1.0f, f == 1.0f);
            }
            if (f < 1) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + f * 0.5f);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.LEGACY_BOW_SHOOT_1, SoundSource.PLAYERS, 1.0f, 0.8f / (level.getRandom().nextFloat() * 0.4f + 1.2f));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.DUNGEONS_BOW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.4f);
            }
        }
    }
}

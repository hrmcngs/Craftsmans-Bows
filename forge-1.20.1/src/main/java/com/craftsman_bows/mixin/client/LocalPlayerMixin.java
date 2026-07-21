package com.craftsman_bows.mixin.client;

import com.craftsman_bows.interfaces.item.CanSprintWhileUsing;
import com.craftsman_bows.interfaces.item.CustomUsingMoveItem;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fabric 版の NoUsingSlowdownMixin / ChangeableUsingMoveSpeed に相当。
 * 1.20.1 ではダッシュ関係の処理が LocalPlayer#aiStep と #canStartSprinting に入っている。
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    /**
     * 右クリックの長押し中だろうがダッシュできるアイテムに関する処理。
     * 「アイテム使用中は移動が遅くなる／ダッシュを開始できない」という判定をまとめて無視する。
     */
    @Redirect(
            method = {"aiStep", "canStartSprinting"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean craftsman_bows$isUsingItem(LocalPlayer instance) {
        if (instance.getUseItem().getItem() instanceof CanSprintWhileUsing) {
            return false;
        }
        return instance.isUsingItem();
    }

    // アイテム使用中の移動速度を変更できる処理（横方向）
    @Redirect(
            method = "aiStep",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/Input;leftImpulse:F", opcode = Opcodes.PUTFIELD))
    private void craftsman_bows$leftImpulse(Input input, float value) {
        input.leftImpulse = value * craftsman_bows$movementSpeed();
    }

    // アイテム使用中の移動速度を変更できる処理（前後方向）
    @Redirect(
            method = "aiStep",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/Input;forwardImpulse:F", opcode = Opcodes.PUTFIELD))
    private void craftsman_bows$forwardImpulse(Input input, float value) {
        input.forwardImpulse = value * craftsman_bows$movementSpeed();

        if (this.getUseItem().getItem() instanceof CustomUsingMoveItem customUsingMoveItem) {
            customUsingMoveItem.resetMovementSpeed();
        }
    }

    @Unique
    private float craftsman_bows$movementSpeed() {
        ItemStack itemStack = this.getUseItem();
        if (itemStack.getItem() instanceof CustomUsingMoveItem customUsingMoveItem) {
            return customUsingMoveItem.getMovementSpeed();
        }
        return 1.0F;
    }
}

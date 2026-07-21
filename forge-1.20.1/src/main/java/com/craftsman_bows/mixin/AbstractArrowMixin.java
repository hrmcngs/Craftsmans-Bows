package com.craftsman_bows.mixin;

import com.craftsman_bows.interfaces.entity.BypassCooldown;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity implements BypassCooldown {

    public AbstractArrowMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Unique
    private static final EntityDataAccessor<Boolean> CRAFTSMAN_BOWS$BYPASS_DAMAGE_COOLDOWN =
            SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BOOLEAN);

    @Override
    public void setBypassDamageCooldown() {
        this.entityData.set(CRAFTSMAN_BOWS$BYPASS_DAMAGE_COOLDOWN, true);
    }

    @Override
    public boolean getBypassDamageCooldown() {
        return this.entityData.get(CRAFTSMAN_BOWS$BYPASS_DAMAGE_COOLDOWN);
    }

    // データトラッカーくんを呼び出す処理
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    protected void craftsman_bows$defineSynchedData(CallbackInfo ci) {
        this.entityData.define(CRAFTSMAN_BOWS$BYPASS_DAMAGE_COOLDOWN, false);
    }

    // ヒット時に無敵時間を剥がす
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    protected void craftsman_bows$forceBypassCooldown(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity entity = entityHitResult.getEntity();
        if (getBypassDamageCooldown() && entity instanceof LivingEntity) {
            entity.invulnerableTime = 0;
        }
    }

    // 地面に刺さったらオフ
    @Inject(method = "onHitBlock", at = @At("TAIL"))
    protected void craftsman_bows$onHitBlock(BlockHitResult blockHitResult, CallbackInfo ci) {
        this.entityData.set(CRAFTSMAN_BOWS$BYPASS_DAMAGE_COOLDOWN, false);
    }

    // NBT に書き込む処理
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void craftsman_bows$addAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean("BypassDamageCooldown", getBypassDamageCooldown());
    }

    // NBT から読み込む処理
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void craftsman_bows$readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        this.entityData.set(CRAFTSMAN_BOWS$BYPASS_DAMAGE_COOLDOWN, nbt.getBoolean("BypassDamageCooldown"));
    }
}

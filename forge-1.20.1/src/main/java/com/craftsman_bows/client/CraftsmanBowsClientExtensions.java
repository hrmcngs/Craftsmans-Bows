package com.craftsman_bows.client;

import com.craftsman_bows.interfaces.item.CustomArmPoseItem;
import com.craftsman_bows.interfaces.item.CustomFirstPersonRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.Objects;

/**
 * 手持ちアイテムの見た目まわり。
 * Fabric 版では PlayerEntityRenderer#getArmPose と HeldItemRenderer#renderFirstPersonItem を
 * Mixin で書き換えていたが、Forge には同等の公式フックがあるのでそちらを使う。
 */
@OnlyIn(Dist.CLIENT)
public class CraftsmanBowsClientExtensions implements IClientItemExtensions {

    public static final CraftsmanBowsClientExtensions INSTANCE = new CraftsmanBowsClientExtensions();

    // ------------------------------------------------------------------
    // 三人称・他プレイヤーから見た腕の構え
    // ------------------------------------------------------------------
    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (!(stack.getItem() instanceof CustomArmPoseItem customArmPoseItem)) {
            return null;
        }

        if (entity.getUseItemRemainingTicks() > 0) {
            return parsePose(customArmPoseItem.getUsingArmPose());
        }
        return parsePose(customArmPoseItem.getStandbyArmPose());
    }

    private static HumanoidModel.ArmPose parsePose(String name) {
        return name == null ? null : HumanoidModel.ArmPose.valueOf(name);
    }

    // ------------------------------------------------------------------
    // 一人称での構え。true を返すとバニラの変形処理はスキップされ、描画だけが行われる。
    // ------------------------------------------------------------------
    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                           ItemStack item, float partialTick, float equipProcess, float swingProcess) {
        if (!player.isUsingItem()) {
            return false;
        }
        if (!(item.getItem() instanceof CustomFirstPersonRender customFirstPersonRender)) {
            return false;
        }

        String mode = customFirstPersonRender.getUsingFirstPersonRender();
        boolean rightHanded = arm == HumanoidArm.RIGHT;
        int i = rightHanded ? 1 : -1;

        // クロスボウ（チャージ中）の構え
        if (Objects.equals(mode, "CROSSBOW_CHARGE")) {
            applyItemArmTransform(poseStack, arm, equipProcess);
            poseStack.translate((float) i * -0.4785682F, -0.094387F, 0.05731531F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-11.935F));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) i * 65.3F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) i * -9.785F));
            float f = (float) item.getUseDuration() - ((float) player.getUseItemRemainingTicks() - partialTick + 1.0F);
            float g = f / (float) CrossbowItem.getChargeDuration(item);
            if (g > 1.0F) {
                g = 1.0F;
            }

            if (g > 0.1F) {
                float h = Mth.sin((f - 0.1F) * 1.3F);
                float j = g - 0.1F;
                float k = h * j;
                poseStack.translate(k * 0.0F, k * 0.004F, k * 0.0F);
            }

            poseStack.translate(g * 0.0F, g * 0.0F, g * 0.04F);
            poseStack.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
            poseStack.mulPose(Axis.YN.rotationDegrees((float) i * 45.0F));
            return true;
        }

        // クロスボウ（チャージ後）の構え
        if (Objects.equals(mode, "CROSSBOW_HOLD")) {
            float fx = -0.4F * Mth.sin(Mth.sqrt(swingProcess) * (float) Math.PI);
            float gx = 0.2F * Mth.sin(Mth.sqrt(swingProcess) * (float) (Math.PI * 2));
            float h = -0.2F * Mth.sin(swingProcess * (float) Math.PI);
            poseStack.translate((float) i * fx, gx, h);
            applyItemArmTransform(poseStack, arm, equipProcess);
            applyItemArmAttackTransform(poseStack, arm, swingProcess);
            if (swingProcess < 0.001F && arm == player.getMainArm()) {
                poseStack.translate((float) i * -0.641864F, 0.0F, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees((float) i * 10.0F));
            }
            return true;
        }

        return false;
    }

    /** ItemInHandRenderer#applyItemArmTransform（private なので同じ内容を持ってきた） */
    private static void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProcess) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float) i * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
    }

    /** ItemInHandRenderer#applyItemArmAttackTransform（private なので同じ内容を持ってきた） */
    private static void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProcess) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        float f = Mth.sin(swingProcess * swingProcess * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) i * (45.0F + f * -20.0F)));
        float f1 = Mth.sin(Mth.sqrt(swingProcess) * (float) Math.PI);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) i * f1 * -20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) i * -45.0F));
    }
}

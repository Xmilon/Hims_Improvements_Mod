package net.xmilon.himproveme.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.entity.custom.DodoEntity;

public class DodoModel<T extends DodoEntity> extends SinglePartEntityModel<T> {
    public static final EntityModelLayer DODO = new EntityModelLayer(Identifier.of(HimProveMe.MOD_ID, "dodo"), "main");
    private final ModelPart bone;
    private final ModelPart rightLeg;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart torso;
    private final ModelPart leftLeg;
    private final ModelPart head;

    public DodoModel(ModelPart root) {
        this.bone = root.getChild("bone");
        this.rightLeg = this.bone.getChild("right_leg");
        this.leftWing = this.bone.getChild("left_wing");
        this.rightWing = this.bone.getChild("right_wing");
        this.torso = this.bone.getChild("torso");
        this.leftLeg = this.bone.getChild("left_leg");
        this.head = this.bone.getChild("head");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData bone = modelPartData.addChild("bone", ModelPartBuilder.create(),
                ModelTransform.pivot(-4.9F, 17.0F, -1.0F));

        bone.addChild("right_leg", ModelPartBuilder.create()
                        .uv(20, 82).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(28, 82).cuboid(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(28, 82).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(20, 82).cuboid(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(28, 82).cuboid(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(20, 82).cuboid(-1.0F, 5.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(19, 82).cuboid(-2.0F, 6.0F, -3.0F, 4.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(20, 82).cuboid(-1.0F, 5.0F, 0.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(28, 82).cuboid(-1.0F, 4.0F, 1.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                        .uv(20, 82).cuboid(-1.0F, 6.0F, -5.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(20, 82).cuboid(-2.0F, 6.0F, -3.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        bone.addChild("left_wing", ModelPartBuilder.create()
                        .uv(66, 50).cuboid(-1.0F, -3.0F, -2.0F, 2.0F, 3.0F, 8.0F, new Dilation(0.0F))
                        .uv(66, 61).cuboid(-1.0F, 0.0F, -2.0F, 2.0F, 3.0F, 8.0F, new Dilation(0.0F))
                        .uv(0, 72).cuboid(-1.0F, 3.0F, -1.0F, 2.0F, 3.0F, 8.0F, new Dilation(0.0F))
                        .uv(40, 72).cuboid(-1.0F, 6.0F, 0.0F, 2.0F, 2.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.pivot(14.0F, -19.0F, -6.0F));

        bone.addChild("right_wing", ModelPartBuilder.create()
                        .uv(20, 72).cuboid(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 8.0F, new Dilation(0.0F))
                        .uv(66, 39).cuboid(-1.0F, -5.0F, -3.0F, 2.0F, 3.0F, 8.0F, new Dilation(0.0F))
                        .uv(66, 28).cuboid(-1.0F, -8.0F, -4.0F, 2.0F, 3.0F, 8.0F, new Dilation(0.0F))
                        .uv(66, 50).cuboid(-1.0F, -11.0F, -4.0F, 2.0F, 3.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-4.0F, -11.0F, -4.0F));

        bone.addChild("torso", ModelPartBuilder.create()
                        .uv(5, 29).cuboid(-8.0F, -7.0F, -10.0F, 16.0F, 7.0F, 12.0F, new Dilation(0.0F))
                        .uv(0, 24).cuboid(-8.0F, 0.0F, -10.0F, 16.0F, 7.0F, 17.0F, new Dilation(0.0F))
                        .uv(2, 2).cuboid(-8.0F, 7.0F, -6.0F, 16.0F, 7.0F, 15.0F, new Dilation(0.0F)),
                ModelTransform.pivot(5.0F, -18.0F, -2.0F));

        bone.addChild("left_leg", ModelPartBuilder.create()
                        .uv(36, 82).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(44, 82).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(36, 82).cuboid(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(44, 82).cuboid(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(36, 82).cuboid(-1.0F, 6.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(44, 82).cuboid(-1.0F, 7.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(20, 82).cuboid(-1.0F, 7.0F, 0.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(19, 82).cuboid(-2.0F, 8.0F, -3.0F, 4.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(20, 82).cuboid(-1.0F, 8.0F, -5.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(20, 82).cuboid(-2.0F, 8.0F, -3.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(10.0F, -2.0F, 0.0F));

        bone.addChild("head", ModelPartBuilder.create()
                        .uv(84, 84).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(86, 21).cuboid(-3.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(86, 29).cuboid(-2.0F, -2.0F, 0.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(86, 37).cuboid(-2.0F, -2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(68, 1).cuboid(-5.0F, -7.0F, -4.0F, 8.0F, 7.0F, 7.0F, new Dilation(0.0F))
                        .uv(61, 76).cuboid(-2.0F, -3.0F, -9.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(60, 76).cuboid(-3.0F, -2.0F, -9.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(60, 76).cuboid(-3.0F, -1.0F, -11.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(61, 76).cuboid(-2.0F, -2.0F, -11.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(62, 77).cuboid(-2.0F, 1.0F, -12.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(62, 77).cuboid(-2.0F, 0.0F, -12.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(61, 76).cuboid(-2.0F, -4.0F, -7.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(60, 76).cuboid(-3.0F, -3.0F, -7.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(61, 76).cuboid(-2.0F, -4.0F, -5.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(60, 76).cuboid(-3.0F, -3.0F, -5.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F))
                        .uv(-8, -5).cuboid(-5.1F, 0.0F, -4.0F, 8.0F, 2.0F, 7.0F, new Dilation(0.0F))
                        .uv(-8, -5).cuboid(-5.1F, 2.0F, -4.0F, 8.0F, 2.0F, 7.0F, new Dilation(0.0F))
                        .uv(-5, -3).cuboid(-4.1F, -8.0F, -3.0F, 6.0F, 2.0F, 5.0F, new Dilation(0.0F)),
                ModelTransform.pivot(6.0F, -29.0F, -6.0F));

        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.bone.traverse().forEach(ModelPart::resetTransform);
        this.updateAnimation(entity.idleAnimationState, DodoAnimations.ANIM_DODO_IDLE, ageInTicks, 1f);
        this.updateAnimation(entity.attackAnimationState, DodoAnimations.ANIM_DODO_ATTACK, ageInTicks, 1f);

        this.head.yaw += netHeadYaw * ((float) Math.PI / 180F);
        this.head.pitch += headPitch * ((float) Math.PI / 180F);

        // Walk cycle aligned with the commented Blockbench leg swings.
        float walkAmount = Math.min(limbSwingAmount, 1.0f);
        float legAmplitude = 0.7417649f; // 42.5 degrees in radians
        float legSwing = MathHelper.cos(limbSwing * 0.9f) * legAmplitude * walkAmount;
        this.rightLeg.pitch += legSwing;
        this.leftLeg.pitch -= legSwing;

        // Visible wing flapping: subtle at idle, stronger while walking.
        float idleFlap = MathHelper.sin(ageInTicks * 0.22f) * 0.08f;
        float walkFlap = MathHelper.cos(limbSwing * 1.2f) * 0.35f * walkAmount;
        this.leftWing.roll -= 0.2617994f + idleFlap + walkFlap;   // flip upward hinge
        this.rightWing.roll += 0.2617994f + idleFlap + walkFlap;  // mirrored upward hinge

        // Extra attack flap synced to hand swing for strong combat feedback.
        float tickDelta = ageInTicks - entity.age;
        float swing = entity.getHandSwingProgress(tickDelta);
        if (swing > 0.0f) {
            float attackFlap = MathHelper.sin(swing * MathHelper.PI) * 0.9f;
            this.leftWing.roll += attackFlap;
            this.rightWing.roll -= attackFlap;
        }

        // Subtle body follow-through while walking.
        this.torso.pitch += MathHelper.cos(limbSwing * 0.9f) * 0.06f * walkAmount;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        this.bone.render(matrices, vertexConsumer, light, overlay);
    }

    public ModelPart getPart() {
        return this.bone;
    }
}

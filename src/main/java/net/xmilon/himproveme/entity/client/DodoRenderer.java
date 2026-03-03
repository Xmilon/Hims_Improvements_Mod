package net.xmilon.himproveme.entity.client;

import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.entity.custom.DodoEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class DodoRenderer extends MobEntityRenderer<DodoEntity, DodoModel<DodoEntity>> {
    private static final float ADULT_SCALE = 0.65f;
    private static final float BABY_SCALE = 0.325f;

    public DodoRenderer(EntityRendererFactory.Context context) {
        super(context, new DodoModel<>(context.getPart(DodoModel.DODO)), 0.45f);
    }

    @Override
    public Identifier getTexture(DodoEntity entity) {
        return Identifier.of(HimProveMe.MOD_ID, "textures/entity/dodo/dodo.png");
    }

    @Override
    public void render(DodoEntity livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {
        if(livingEntity.isBaby()) {
            matrixStack.scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
        } else {
            matrixStack.scale(ADULT_SCALE, ADULT_SCALE, ADULT_SCALE);
        }

        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}

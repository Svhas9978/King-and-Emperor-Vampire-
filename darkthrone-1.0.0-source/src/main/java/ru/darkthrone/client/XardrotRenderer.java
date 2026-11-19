package ru.darkthrone.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import ru.darkthrone.DarkThrone;
import ru.darkthrone.entity.XardrotEntity;

public class XardrotRenderer extends MobRenderer<XardrotEntity, BipedModel<XardrotEntity>> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation(DarkThrone.MOD_ID, "textures/entity/xardrot_phase1.png"),
            new ResourceLocation(DarkThrone.MOD_ID, "textures/entity/xardrot_phase2.png"),
            new ResourceLocation(DarkThrone.MOD_ID, "textures/entity/xardrot_phase3.png")
    };

    public XardrotRenderer(EntityRendererManager manager) {
        super(manager, new BipedModel<XardrotEntity>(0.0F), 1.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(XardrotEntity entity) {
        int phase = Math.max(1, Math.min(3, entity.getPhase()));
        return TEXTURES[phase - 1];
    }

    @Override
    protected void scale(XardrotEntity entity, MatrixStack matrix, float partialTicks) {
        matrix.scale(1.8F, 1.8F, 1.8F);
    }
}

package net.eonzenx.spool_ge.mixin;

import net.eonzenx.spool_ge.SpoolGameplayEdits;
import net.eonzenx.spool_ge.config.Config;
import net.eonzenx.spool_ge.entities.items.SGEElytraItem;
import net.eonzenx.spool_ge.utils.mixin.renderer.IElytraRender;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ElytraFeatureRenderer.class)
public abstract class SGEElytraFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> implements IElytraRender<T>
{
    public SGEElytraFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Shadow @Final private static Identifier SKIN;
    @Shadow @Final private ElytraEntityModel<T> elytra;

    private static final Identifier LEAF_WINGS_SKIN = SpoolGameplayEdits.newId("textures/entity/leaf_wings.png");
    private static final Identifier FEATHER_WINGS_SKIN = SpoolGameplayEdits.newId("textures/entity/feather_wings.png");
    private static final Identifier HIDE_WINGS_SKIN = SpoolGameplayEdits.newId("textures/entity/hide_wings.png");


    public Identifier getSkinId(ElytraItem elytraItem) {
        if (elytraItem instanceof SGEElytraItem sgeElytraItem) {
            var material = sgeElytraItem.getMaterial();

            if (material == ToolMaterials.WOOD) {
                return LEAF_WINGS_SKIN;
            } else if (material == ToolMaterials.STONE) {
                return FEATHER_WINGS_SKIN;
            } else if (material == ToolMaterials.IRON) {
                return HIDE_WINGS_SKIN;
            }
        }

        return SKIN;
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        var itemStack = livingEntity.getEquippedStack(EquipmentSlot.CHEST);
        var item = livingEntity.getEquippedStack(EquipmentSlot.CHEST).getItem();

        if (item instanceof ElytraItem elytraItem) {
            Identifier identifier4;
            if (livingEntity instanceof AbstractClientPlayerEntity abstractClientPlayerEntity) {
                if (abstractClientPlayerEntity.canRenderElytraTexture() && abstractClientPlayerEntity.getElytraTexture() != null) {
                    identifier4 = abstractClientPlayerEntity.getElytraTexture();
                } else if (abstractClientPlayerEntity.canRenderCapeTexture() && abstractClientPlayerEntity.getCapeTexture() != null && abstractClientPlayerEntity.isPartVisible(PlayerModelPart.CAPE)) {
                    identifier4 = abstractClientPlayerEntity.getCapeTexture();
                } else {
                    identifier4 = getSkinId(elytraItem);
                }
            } else {
                identifier4 = getSkinId(elytraItem);
            }

            matrixStack.push();
            matrixStack.translate(0.0D, 0.0D, 0.125D);
            this.getContextModel().copyStateTo(this.elytra);
            this.elytra.setAngles(livingEntity, f, g, j, k, l);
            VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(identifier4), false, itemStack.hasGlint());
            this.elytra.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.pop();
        }
    }
}

package cy.jdkdigital.productivemetalworks.integration.jei.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EntityRenderer
{
    public static Map<ResourceLocation, Entity> cache = new HashMap<>();

    @Nullable
    public static Entity get(ResourceLocation entityId, Minecraft minecraft) {
        return entityId.equals(ResourceLocation.parse("minecraft:player")) ? minecraft.player : cache.getOrDefault(entityId, null);
    }

    public static void render(GuiGraphics guiGraphics, int xPosition, int yPosition, ResourceLocation entityId, Minecraft minecraft) {
        if (!cache.containsKey(entityId) && minecraft.level != null) {
            cache.put(entityId, BuiltInRegistries.ENTITY_TYPE.get(entityId).create(minecraft.level));
        }

        render(guiGraphics, xPosition, yPosition, get(entityId, minecraft), minecraft);
    }

    public static void render(GuiGraphics guiGraphics, int xPosition, int yPosition, @Nullable Entity entity, Minecraft minecraft) {
        if (minecraft.player != null && entity != null) {
            var data = entity.getType().builtInRegistryHolder().getData(MetalworksRegistrator.ENTITY_MELTING_MAP);
            float scaledSize = 18 * (data != null ? data.scale() : 1.0f);

            entity.tickCount = minecraft.player.tickCount;
            entity.setYBodyRot(-20);

            PoseStack postStack = guiGraphics.pose();
            postStack.pushPose();
            postStack.translate(7D + xPosition, 12D + yPosition, 1.5);
            postStack.mulPose(Axis.ZP.rotationDegrees(190.0F));
            postStack.mulPose(Axis.YP.rotationDegrees(20.0F));
            postStack.mulPose(Axis.XP.rotationDegrees(20.0F));
            postStack.translate(0.0F, -0.2F, 1);
            postStack.scale(scaledSize, scaledSize, scaledSize);

            EntityRenderDispatcher entityRendererManager = minecraft.getEntityRenderDispatcher();
            MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
            entityRendererManager.render(entity, 0, 0, 0.0D, minecraft.getFrameTimeNs(), 1, postStack, buffer, 15728880);
            postStack.popPose();
        }
    }
}

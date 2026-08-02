package cy.jdkdigital.productivemetalworks.integration.jei.ingredient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EntityRenderer
{
    public static Map<Identifier, Entity> cache = new HashMap<>();

    @Nullable
    public static Entity get(Identifier entityId, Minecraft minecraft) {
        return entityId.equals(Identifier.parse("minecraft:player")) ? minecraft.player : cache.getOrDefault(entityId, null);
    }

    public static void render(GuiGraphicsExtractor guiGraphics, int xPosition, int yPosition, Identifier entityId, Minecraft minecraft) {
        if (!cache.containsKey(entityId) && minecraft.level != null) {
            Entity entity = BuiltInRegistries.ENTITY_TYPE.get(entityId)
                    .map(Holder::value)
                    .map(type -> type.create(minecraft.level, EntitySpawnReason.LOAD))
                    .orElse(null);
            cache.put(entityId, entity);
        }

        render(guiGraphics, xPosition, yPosition, get(entityId, minecraft), minecraft);
    }

    // 26.1: live entity preview now goes through the extract/submit render-state pipeline —
    // EntityRenderDispatcher#extractEntity produces an EntityRenderState that GuiGraphicsExtractor#entity submits.
    public static void render(GuiGraphicsExtractor guiGraphics, int xPosition, int yPosition, @Nullable Entity entity, Minecraft minecraft) {
        if (entity == null) {
            return;
        }
        EntityRenderState renderState = minecraft.getEntityRenderDispatcher().extractEntity(entity, 0.0F);

        float bbMax = Math.max(entity.getBbWidth(), entity.getBbHeight());
        float scale = 16.0F / Math.max(bbMax, 0.001F) * 0.85F;

        // Face the viewer; the default GUI entity camera looks 30° down, so cancel that out.
        Quaternionf cameraAngle = new Quaternionf().rotationXYZ(0.0F, Mth.PI, Mth.PI);
        Vector3f translation = new Vector3f(0.0F, entity.getBbHeight() * -0.5F, 0.0F);
        Quaternionf rotation = new Quaternionf().rotationY(Mth.PI);

        guiGraphics.entity(renderState, scale, translation, rotation, cameraAngle, xPosition - 16, yPosition - 16, xPosition + 16, yPosition + 16);
    }
}

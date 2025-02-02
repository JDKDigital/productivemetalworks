package cy.jdkdigital.productivemetalworks.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cy.jdkdigital.productivelib.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

public class RenderHelper
{
    public static float pixelFraction = 0.0625f; // fractional size of 1 pixel
    public static float halfPixelFraction = 0.03125f;

    public static void renderFullFluidLayer(PoseStack poseStack, VertexConsumer vertexBuffer, float fluidYStart, float fluidYEnd, int[] xRange, int[] zRange, FluidStack fluidStack, int combinedLightIn, int combinedOverlayIn, float opacity, float shrinkage) {
        Matrix4f lastPose = poseStack.last().pose();

        //fluid colour tint info
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        int fluidTintColour = renderProperties.getTintColor(fluidStack);
        float[] color = ColorUtil.getCacheColor(fluidTintColour);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(renderProperties.getStillTexture(fluidStack));

        // Adjust opacity of fluid for casting
        color[3] = opacity;

        for (int xd: xRange) {
            for (int zd: zRange) {
                float x1 = pixelFraction * 16f * xd;
                float x2 = pixelFraction * 16f * (xd + 1);
                float z1 = pixelFraction * 16f * zd;
                float z2 = pixelFraction * 16f * (zd + 1);

                renderFluidVertex(vertexBuffer, lastPose, Direction.UP, x1, x2, fluidYEnd, fluidYEnd, z1, z2, color, sprite, combinedLightIn, combinedOverlayIn);

                if (fluidYStart != fluidYEnd && xd == xRange[0] || xd == xRange[xRange.length - 1] || zd == zRange[0] || zd == zRange[zRange.length - 1]) {
                    // Render a block for each y level
                    int yOffset = (int)Math.floor(fluidYStart); // 1 - 2
                    for (int y = yOffset; y < Math.ceil(fluidYEnd - fluidYStart) + yOffset; y++) {
                        float actualYStart = fluidYStart + y - yOffset;
                        float actualYEnd = fluidYEnd;
                        // If there's more than 1 block height left to render, clamp it to 1
                        if (fluidYEnd - actualYStart >= 1) {
                            actualYEnd = actualYStart + 1;
                        }
                        renderFluidVertex(vertexBuffer, lastPose, Direction.EAST, x1 + shrinkage, x2 - shrinkage, actualYStart, actualYEnd, z1 + shrinkage, z2 - shrinkage, color, sprite, combinedLightIn, combinedOverlayIn);
                        renderFluidVertex(vertexBuffer, lastPose, Direction.NORTH, x1 + shrinkage, x2 - shrinkage, actualYStart, actualYEnd, z1 + shrinkage, z2 - shrinkage, color, sprite, combinedLightIn, combinedOverlayIn);
                        renderFluidVertex(vertexBuffer, lastPose, Direction.SOUTH, x1 + shrinkage, x2 - shrinkage, actualYStart, actualYEnd, z1 + shrinkage, z2 - shrinkage, color, sprite, combinedLightIn, combinedOverlayIn);
                        renderFluidVertex(vertexBuffer, lastPose, Direction.WEST, x1 + shrinkage, x2 - shrinkage, actualYStart, actualYEnd, z1 + shrinkage, z2 - shrinkage, color, sprite, combinedLightIn, combinedOverlayIn);
                    }
                }
            }
        }
    }

    public static void renderCenteredFluidColumn(PoseStack poseStack, VertexConsumer vertexBuffer, float width, float y1, float y2, FluidStack fluidStack, int combinedLightIn, int combinedOverlayIn, float opacity) {
        Matrix4f lastPose = poseStack.last().pose();

        //fluid colour tint info
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        int fluidTintColour = renderProperties.getTintColor(fluidStack);
        float[] color = ColorUtil.getCacheColor(fluidTintColour);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(renderProperties.getStillTexture(fluidStack));

        // Adjust opacity of fluid for casting
        color[3] = opacity;

        float offset = ((16f - width) / 2);
        float x1 = pixelFraction * offset;
        float x2 = pixelFraction * (16f - offset);
        float z1 = pixelFraction * offset;
        float z2 = pixelFraction * (16f - offset);

        renderFluidVertex(vertexBuffer, lastPose, Direction.EAST, x1, x2, y1, y2, z1, z2, color, sprite, combinedLightIn, combinedOverlayIn);
        renderFluidVertex(vertexBuffer, lastPose, Direction.NORTH, x1, x2, y1, y2, z1, z2, color, sprite, combinedLightIn, combinedOverlayIn);
        renderFluidVertex(vertexBuffer, lastPose, Direction.SOUTH, x1, x2, y1, y2, z1, z2, color, sprite, combinedLightIn, combinedOverlayIn);
        renderFluidVertex(vertexBuffer, lastPose, Direction.WEST, x1, x2, y1, y2, z1, z2, color, sprite, combinedLightIn, combinedOverlayIn);
    }

    public static void renderFluidVertex(VertexConsumer vertexBuffer, Matrix4f lastPose, Direction dir, float x1, float x2, float y1, float y2, float z1, float z2, float[] color, TextureAtlasSprite sprite, int combinedLightIn, int combinedOverlayIn) {
        float xd = (Math.max(x2, x1) - Math.min(x2, x1)) / 16f;
        float zd = (Math.max(z2, z1) - Math.min(z2, z1)) / 16f;
        float yd = (Math.max(y2, y1) - Math.min(y2, y1)) / 16f;
        switch (dir) {
            case NORTH:
                vertexBuffer.addVertex(lastPose, x1, y1, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(0f), sprite.getV(yd * 16f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x1, y2, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(0f), sprite.getV(0f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x2, y2, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(xd * 16f), sprite.getV(0f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x2, y1, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(xd * 16f), sprite.getV(yd * 16f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                break;
            case SOUTH:
                vertexBuffer.addVertex(lastPose, x2, y1, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(0f), sprite.getV(yd * 16f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x2, y2, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(0f), sprite.getV(0f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x1, y2, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(xd * 16f), sprite.getV(0f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x1, y1, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(xd * 16f), sprite.getV(yd * 16f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                break;
            case WEST:
                vertexBuffer.addVertex(lastPose, x1, y1, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(0f), sprite.getV(yd * 16f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x1, y2, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(0f), sprite.getV(0f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x1, y2, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(zd * 16f), sprite.getV(0f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x1, y1, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(zd * 16f), sprite.getV(yd * 16f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                break;
            case EAST:
                vertexBuffer.addVertex(lastPose, x2, y1, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(0f), sprite.getV(yd * 16f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x2, y2, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(0f), sprite.getV(0f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x2, y2, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(zd * 16f), sprite.getV(0f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x2, y1, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU(zd * 16f), sprite.getV(yd * 16f)).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                break;
            default:
                vertexBuffer.addVertex(lastPose, x1, y1, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU0(), sprite.getV1()).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x1, y1, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU0(), sprite.getV0()).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x2, y2, z2).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU1(), sprite.getV0()).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
                vertexBuffer.addVertex(lastPose, x2, y2, z1).setColor(color[0], color[1], color[2], color[3]).setUv(sprite.getU1(), sprite.getV1()).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0, 1, 0);
        }
    }
}

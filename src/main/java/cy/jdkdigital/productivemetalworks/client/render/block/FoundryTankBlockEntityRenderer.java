package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTankBlockEntity;
import cy.jdkdigital.productivemetalworks.util.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public class FoundryTankBlockEntityRenderer implements BlockEntityRenderer<FoundryTankBlockEntity>
{
    public FoundryTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(FoundryTankBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        if (blockEntity.getLevel() != null) {
            FluidStack fluidStack = blockEntity.getFluidHandler().getFluid();
            if (!fluidStack.isEmpty()) {
                poseStack.pushPose();

                //fluid texture info
                VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));

                float fillProgress = (float) fluidStack.getAmount() / (float) blockEntity.getFluidHandler().getCapacity();
                float fluidY = RenderHelper.halfPixelFraction + (fillProgress * 15.01f) * RenderHelper.pixelFraction;

                var range = IntStream.range(0, 1).toArray();

                RenderHelper.renderFullFluidLayer(poseStack, vertexBuffer, 0, fluidY, range, range, fluidStack, combinedLightIn, combinedOverlayIn, 1.0f, 0.01f);

                poseStack.popPose();
            }
        }
    }
}
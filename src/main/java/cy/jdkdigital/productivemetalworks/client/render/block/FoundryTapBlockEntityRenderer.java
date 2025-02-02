package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTapBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FoundryTapBlockEntityRenderer implements BlockEntityRenderer<FoundryTapBlockEntity>
{
    public FoundryTapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(FoundryTapBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        if (blockEntity.getLevel() != null && blockEntity.isActive) {
            FluidStack fluidStack = new FluidStack(BuiltInRegistries.FLUID.byId(blockEntity.fluidId), 1);
            if (!fluidStack.isEmpty()) {
                poseStack.pushPose();

                //fluid texture info
                VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));

                float fluidY = RenderHelper.halfPixelFraction + 5f * RenderHelper.pixelFraction;

                RenderHelper.renderCenteredFluidColumn(poseStack, vertexBuffer, 3f, 0, fluidY, fluidStack, combinedLightIn, combinedOverlayIn, 1.0f);

                if (blockEntity.getLevel().getBlockState(blockEntity.getBlockPos().below()).is(MetalworksRegistrator.CASTING_BASIN)) {
                    RenderHelper.renderCenteredFluidColumn(poseStack, vertexBuffer, 3f, -0.99f, 0, fluidStack, combinedLightIn, combinedOverlayIn, 1.0f);
                }

                poseStack.popPose();
            }
        }
    }
}
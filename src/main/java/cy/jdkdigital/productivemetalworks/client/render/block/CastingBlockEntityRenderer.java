package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import cy.jdkdigital.productivemetalworks.common.block.entity.CastingBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.RenderHelper;
import cy.jdkdigital.productivemetalworks.util.TintedItemRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public class CastingBlockEntityRenderer implements BlockEntityRenderer<CastingBlockEntity>
{
    public CastingBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(CastingBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        if (blockEntity.getLevel() != null ) {
            var isTable = blockEntity.getBlockState().is(MetalworksRegistrator.CASTING_TABLE.get());

            float recipeProgress = blockEntity.coolingTime > 0 ? ((float)blockEntity.coolingTime / (float)blockEntity.maxAmount) : 1.0f;

            FluidStack fluidStack = blockEntity.getFluidHandler().getFluid();
            if (!fluidStack.isEmpty()) {
                poseStack.pushPose();

                //fluid texture info
                VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));

                float fillProgress = (float) fluidStack.getAmount() / (float) blockEntity.maxAmount;
                float fluidY = (isTable ? 0.9375f : RenderHelper.pixelFraction) + (fillProgress * (isTable ? 0.7f : 14.01f)) * RenderHelper.pixelFraction;

                var range = IntStream.range(0, 1).toArray();

                RenderHelper.renderFullFluidLayer(poseStack, vertexBuffer, fluidY, fluidY, range, range, fluidStack, combinedLightIn, combinedOverlayIn, recipeProgress, 0.001f);

                poseStack.popPose();
            }
            // Render cast item
            ItemStack cast = blockEntity.castInv.getStackInSlot(0);
            if (!cast.isEmpty()) {
                poseStack.pushPose();
                if (isTable) {
                    poseStack.translate(0.5f, 0.96f, 0.5f);
                    poseStack.mulPose(Axis.XP.rotationDegrees((float) (90.0D % 360)));
                } else {
                    if (!(cast.getItem() instanceof BlockItem)) {
                        poseStack.translate(0.5f, 0.5f, 0.5f);
                        poseStack.mulPose(Axis.YP.rotationDegrees((float) (30.0D % 360)));
                        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (180.0D % 360)));
                    }
                    poseStack.translate(RenderHelper.pixelFraction, RenderHelper.pixelFraction, RenderHelper.pixelFraction);
                }
                poseStack.scale(0.875f, 0.875f, 0.875f);
                if (cast.getItem() instanceof BlockItem blockItem) {
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockItem.getBlock().defaultBlockState(), poseStack, bufferSource,combinedLightIn, combinedOverlayIn, ModelData.EMPTY, null);
                } else {
                    Minecraft.getInstance().getItemRenderer().renderStatic(cast, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, poseStack, bufferSource, blockEntity.getLevel(), 0);
                }
                poseStack.popPose();
            }
            // Render output item
            ItemStack output = blockEntity.getItemHandler().getStackInSlot(0);
            if (!output.isEmpty()) {
                if (blockEntity.coolingTime > 0) {
                    bufferSource = new TintedItemRenderTypeBuffer(bufferSource, (int) (4 * 0xFF * (1f - recipeProgress)), (int) (4 * 0xFF * (recipeProgress)));
                }
                poseStack.pushPose();
                if (isTable) {
                    poseStack.scale(0.98f, 0.99f, 0.98f);
                    poseStack.translate(0.51f, 0.965f, 0.51f);
                    poseStack.mulPose(Axis.XP.rotationDegrees((float) (90.0D % 360)));
                } else {
                    poseStack.translate(RenderHelper.pixelFraction, RenderHelper.pixelFraction, RenderHelper.pixelFraction);
                    poseStack.scale(0.875f, 0.875f, 0.875f);
                }
                if (output.getItem() instanceof BlockItem blockItem) {
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockItem.getBlock().defaultBlockState(), poseStack, bufferSource,combinedLightIn, combinedOverlayIn, ModelData.EMPTY, null);
                } else {
                    Minecraft.getInstance().getItemRenderer().renderStatic(output, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, poseStack, bufferSource, blockEntity.getLevel(), 0);
                }
                poseStack.popPose();
            }
        }
    }
}
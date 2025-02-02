package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
import cy.jdkdigital.productivemetalworks.util.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public class FoundryControllerBlockEntityRenderer implements BlockEntityRenderer<FoundryControllerBlockEntity>
{
    public FoundryControllerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(FoundryControllerBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        if (blockEntity.getMultiblockData() != null && blockEntity.getBlockState().getValue(BlockStateProperties.ATTACHED)) {
            var multiBlockData = blockEntity.getMultiblockData();
            BlockPos c1 = multiBlockData.topCorners().getFirst();
            BlockPos c2 = multiBlockData.topCorners().getSecond();
            int xl1 = c1.getX() - multiBlockData.controllerPos().getX();
            int xl2 = c2.getX() - multiBlockData.controllerPos().getX();
            var xRange = IntStream.range(Math.min(xl1, xl2) + 1, Math.max(xl1, xl2)).toArray();
            int zl1 = c1.getZ() - multiBlockData.controllerPos().getZ();
            int zl2 = c2.getZ() - multiBlockData.controllerPos().getZ();
            var zRange = IntStream.range(Math.min(zl1, zl2) + 1, Math.max(zl1, zl2)).toArray();

            float fillPercentage = (float) blockEntity.fluidHandler.totalFluidAmount() / (float) blockEntity.fluidHandler.getCapacity();
            float fluidHeight = .0001f;
            for (int tank = 0; tank < blockEntity.fluidHandler.getTanks(); tank++) {
                // render from bottom to top
                var fluidStack = blockEntity.fluidHandler.getFluidInTank(tank);
                if (blockEntity.getLevel() != null && !fluidStack.isEmpty()) {
                    poseStack.pushPose();

                    int blocksFromBottom = multiBlockData.height() - (c1.getY() - multiBlockData.controllerPos().getY());

                    //fluid texture info
                    VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));

                    float fluidFillPercentage = (float)fluidStack.getAmount() / (float)blockEntity.fluidHandler.totalFluidAmount() * 16f;
                    float fluidYend = fluidHeight + (fillPercentage * (multiBlockData.height() * fluidFillPercentage - 1)) * RenderHelper.pixelFraction;

                    float offsetY = -1f * (blocksFromBottom - 1);
                    poseStack.translate(0, offsetY, 0);

                    RenderHelper.renderFullFluidLayer(poseStack, vertexBuffer, fluidHeight, fluidYend, xRange, zRange, fluidStack, combinedLightIn, combinedOverlayIn, 1.0f, 0.001f);

                    fluidHeight = fluidYend;

                    poseStack.popPose();
                }
            }

            // Render inventory
            var bb = new BoundingBox(
                    Math.min(c1.getX(), c2.getX()),
                    Math.min(c1.getY() + 1, c2.below(multiBlockData.height()).getY()),
                    Math.min(c1.getZ(), c2.getZ()),
                    Math.max(c1.getX(), c2.getX()),
                    Math.max(c1.getY() + 1, c2.below(multiBlockData.height()).getY()),
                    Math.max(c1.getZ(), c2.getZ())
            );
            var positions = BlockPos.betweenClosedStream(bb.inflatedBy(-1)).map(BlockPos::immutable).toList();
            for (int slot = 0; slot < blockEntity.getItemHandler().getSlots(); slot++) {
                ItemStack output = blockEntity.getItemHandler().getStackInSlot(slot);
                if (!output.isEmpty() && slot < positions.size()) {
                    var pos = positions.get(slot);
                    poseStack.pushPose();
                    poseStack.translate(pos.getX() - multiBlockData.controllerPos().getX() + RenderHelper.halfPixelFraction, pos.getY() - multiBlockData.controllerPos().getY() + RenderHelper.halfPixelFraction, pos.getZ() - multiBlockData.controllerPos().getZ() + RenderHelper.halfPixelFraction);
                    poseStack.scale(1.0f - RenderHelper.pixelFraction, 1.0f - RenderHelper.pixelFraction, 1.0f - RenderHelper.pixelFraction);
                    if (output.getItem() instanceof BlockItem blockItem) {
                        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockItem.getBlock().defaultBlockState(), poseStack, bufferSource, combinedLightIn, combinedOverlayIn, ModelData.EMPTY, null);
                    } else {
                        poseStack.translate(0.5f, 0.5f, 0.5f);
                        poseStack.mulPose(Axis.XP.rotationDegrees((float) (90.0D % 360)));
                        Minecraft.getInstance().getItemRenderer().renderStatic(output, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, poseStack, bufferSource, blockEntity.getLevel(), 0);
                    }
                    poseStack.popPose();
                }
            }
        }
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public AABB getRenderBoundingBox(FoundryControllerBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
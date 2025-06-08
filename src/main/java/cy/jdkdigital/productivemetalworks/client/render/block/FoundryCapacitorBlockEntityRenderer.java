package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryCapacitorBlockEntity;
import cy.jdkdigital.productivemetalworks.util.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FoundryCapacitorBlockEntityRenderer implements BlockEntityRenderer<FoundryCapacitorBlockEntity>
{
    static ResourceLocation POWER = ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "block/power_level");

    public FoundryCapacitorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(FoundryCapacitorBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        if (blockEntity.getLevel() != null && blockEntity.getEnergyHandler().getMaxEnergyStored() > 0) {
            Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            float f = 12f - (12f * blockEntity.getEnergyHandler().getEnergyStored() / (float) blockEntity.getEnergyHandler().getMaxEnergyStored());

            poseStack.pushPose();
            VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.cutout());
            RenderHelper.renderCenteredTexturedSide(poseStack, vertexBuffer, facing, POWER, 6f, 1.99f * RenderHelper.pixelFraction, (14f - f) * RenderHelper.pixelFraction, combinedLightIn, combinedOverlayIn);
            poseStack.popPose();
        }
    }
}
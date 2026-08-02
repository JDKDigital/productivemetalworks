package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.productivelib.client.FluidRenderHelper;
import cy.jdkdigital.productivelib.common.block.entity.InventoryHandlerHelper;
import cy.jdkdigital.productivemetalworks.common.block.entity.CastingBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class CastingBlockEntityRenderer implements BlockEntityRenderer<CastingBlockEntity, CastingBlockEntityRenderer.CastingRenderState>
{
    private static final int[] SINGLE = new int[]{0};

    private final ItemModelResolver itemModelResolver;

    public CastingBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public CastingRenderState createRenderState() {
        return new CastingRenderState();
    }

    @Override
    public void extractRenderState(CastingBlockEntity be, CastingRenderState state, float partialTicks, @Nonnull net.minecraft.world.phys.Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);

        state.isTable = be.getBlockState().is(MetalworksRegistrator.CASTING_TABLE.get());
        state.recipeProgress = be.coolingTime > 0 ? ((float) be.coolingTime / (float) be.maxAmount) : 1.0f;

        FluidStack fluidStack = be.getFluidHandler().getFluid();
        state.hasFluid = !fluidStack.isEmpty();
        if (state.hasFluid) {
            state.fluidStack = fluidStack.copy();
            float fillProgress = (float) fluidStack.getAmount() / (float) be.maxAmount;
            state.fluidY = (state.isTable ? 0.9375f : FluidRenderHelper.pixelFraction) + (fillProgress * (state.isTable ? 0.7f : 14.01f)) * FluidRenderHelper.pixelFraction;
        }

        ItemStack cast = be.castInv.getStackInSlot(0);
        state.hasCast = !cast.isEmpty();
        if (state.hasCast) {
            state.castIsBlockItem = cast.getItem() instanceof BlockItem;
            this.itemModelResolver.updateForTopItem(state.castState, cast, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
        }

        ItemStack output = ((InventoryHandlerHelper.BlockEntityItemStackHandler) be.getItemHandler()).getStackInSlot(0);
        state.hasOutput = !output.isEmpty();
        if (state.hasOutput) {
            state.outputIsBlockItem = output.getItem() instanceof BlockItem;
            this.itemModelResolver.updateForTopItem(state.outputState, output, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(CastingRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState cameraState) {
        if (state.hasFluid) {
            poseStack.pushPose();
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) ->
                    FluidRenderHelper.renderFullFluidLayer(pose.pose(), buffer, state.fluidY, state.fluidY, SINGLE, SINGLE, state.fluidStack, state.lightCoords, OverlayTexture.NO_OVERLAY, state.recipeProgress, 0.001f));
            poseStack.popPose();
        }
        // Render cast item
        if (state.hasCast) {
            poseStack.pushPose();
            if (state.isTable) {
                poseStack.translate(0.5f, 0.96f, 0.5f);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
                poseStack.scale(0.875f, 0.875f, 0.875f);
            } else if (state.castIsBlockItem) {
                poseStack.translate(0.5f, 0.5f, 0.5f);
                poseStack.scale(1.75f, 1.75f, 1.75f);
            } else {
                poseStack.translate(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(30.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
                poseStack.translate(FluidRenderHelper.pixelFraction, FluidRenderHelper.pixelFraction, FluidRenderHelper.pixelFraction);
                poseStack.scale(0.875f, 0.875f, 0.875f);
            }
            state.castState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        // Render output item
        if (state.hasOutput) {
            poseStack.pushPose();
            if (state.isTable) {
                poseStack.scale(0.98f, 0.99f, 0.98f);
                poseStack.translate(0.51f, 0.965f, 0.51f);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            } else if (state.outputIsBlockItem) {
                poseStack.translate(0.5f, 0.5f, 0.5f);
                poseStack.scale(1.75f, 1.75f, 1.75f);
            } else {
                poseStack.translate(FluidRenderHelper.pixelFraction, FluidRenderHelper.pixelFraction, FluidRenderHelper.pixelFraction);
                poseStack.scale(0.875f, 0.875f, 0.875f);
            }
            state.outputState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static class CastingRenderState extends BlockEntityRenderState
    {
        public boolean isTable;
        public float recipeProgress = 1.0f;
        public boolean hasFluid;
        public FluidStack fluidStack = FluidStack.EMPTY;
        public float fluidY;
        public boolean hasCast;
        public boolean castIsBlockItem;
        public final ItemStackRenderState castState = new ItemStackRenderState();
        public boolean hasOutput;
        public boolean outputIsBlockItem;
        public final ItemStackRenderState outputState = new ItemStackRenderState();
    }
}

package cy.jdkdigital.productivemetalworks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cy.jdkdigital.productivelib.client.FluidRenderHelper;
import cy.jdkdigital.productivelib.common.block.entity.InventoryHandlerHelper;
import cy.jdkdigital.productivemetalworks.Config;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class FoundryControllerBlockEntityRenderer implements BlockEntityRenderer<FoundryControllerBlockEntity, FoundryControllerBlockEntityRenderer.FoundryRenderState>
{
    private final ItemModelResolver itemModelResolver;

    public FoundryControllerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public FoundryRenderState createRenderState() {
        return new FoundryRenderState();
    }

    @Override
    public void extractRenderState(FoundryControllerBlockEntity be, FoundryRenderState state, float partialTicks, @Nonnull Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);

        state.fluidLayers.clear();
        state.invItems.clear();
        state.render = be.getMultiblockData() != null && be.getBlockState().getValue(BlockStateProperties.ATTACHED) && be.getLevel() != null;
        if (!state.render) {
            return;
        }

        var multiBlockData = be.getMultiblockData();
        BlockPos c1 = multiBlockData.topCorners().getFirst();
        BlockPos c2 = multiBlockData.topCorners().getSecond();
        int xl1 = c1.getX() - multiBlockData.controllerPos().getX();
        int xl2 = c2.getX() - multiBlockData.controllerPos().getX();
        state.xRange = IntStream.range(Math.min(xl1, xl2) + 1, Math.max(xl1, xl2)).toArray();
        int zl1 = c1.getZ() - multiBlockData.controllerPos().getZ();
        int zl2 = c2.getZ() - multiBlockData.controllerPos().getZ();
        state.zRange = IntStream.range(Math.min(zl1, zl2) + 1, Math.max(zl1, zl2)).toArray();

        float fillPercentage = (float) be.fluidHandler.totalFluidAmount() / (float) be.fluidHandler.getCapacity();
        float fluidHeight = .0001f;
        int blocksFromBottom = multiBlockData.height() - (c1.getY() - multiBlockData.controllerPos().getY());
        for (int tank = 0; tank < be.fluidHandler.getTanks(); tank++) {
            FluidStack fluidStack = be.fluidHandler.getFluidInTank(tank);
            if (!fluidStack.isEmpty()) {
                float fluidFillPercentage = (float) fluidStack.getAmount() / (float) be.fluidHandler.totalFluidAmount() * 16f;
                float fluidYend = fluidHeight + (fillPercentage * (multiBlockData.height() * fluidFillPercentage - 1)) * FluidRenderHelper.pixelFraction;
                state.fluidLayers.add(new FluidLayer(fluidStack.copy(), fluidHeight, fluidYend, -1f * (blocksFromBottom - 1)));
                fluidHeight = fluidYend;
            }
        }

        if (Config.foundryRenderInventory && be.getItemHandler() instanceof InventoryHandlerHelper.BlockEntityItemStackHandler itemHandler) {
            var aabb = new BoundingBox(
                    Math.min(c1.getX(), c2.getX()) + 1,
                    c1.below(multiBlockData.height() - 1).getY(),
                    Math.min(c1.getZ(), c2.getZ()) + 1,
                    Math.max(c1.getX(), c2.getX()) - 1,
                    c1.getY(),
                    Math.max(c1.getZ(), c2.getZ()) - 1
            );
            var positions = BlockPos.betweenClosedStream(aabb).map(BlockPos::immutable).toList();
            for (int slot = 0; slot < itemHandler.size(); slot++) {
                ItemStack output = itemHandler.getStackInSlot(slot);
                if (!output.isEmpty() && slot < positions.size()) {
                    var pos = positions.get(slot);
                    InvItem invItem = new InvItem();
                    invItem.x = pos.getX() - multiBlockData.controllerPos().getX() + FluidRenderHelper.halfPixelFraction;
                    invItem.y = pos.getY() - multiBlockData.controllerPos().getY() + FluidRenderHelper.halfPixelFraction;
                    invItem.z = pos.getZ() - multiBlockData.controllerPos().getZ() + FluidRenderHelper.halfPixelFraction;
                    invItem.isBlockItem = output.getItem() instanceof BlockItem;
                    this.itemModelResolver.updateForTopItem(invItem.stackState, output, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
                    state.invItems.add(invItem);
                }
            }
        }
    }

    @Override
    public void submit(FoundryRenderState state, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState cameraState) {
        if (!state.render) {
            return;
        }
        for (FluidLayer layer : state.fluidLayers) {
            poseStack.pushPose();
            poseStack.translate(0, layer.offsetY, 0);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) ->
                    FluidRenderHelper.renderFullFluidLayer(pose.pose(), buffer, layer.yStart, layer.yEnd, state.xRange, state.zRange, layer.stack, state.lightCoords, OverlayTexture.NO_OVERLAY, 1.0f, 0.001f));
            poseStack.popPose();
        }

        for (InvItem invItem : state.invItems) {
            poseStack.pushPose();
            poseStack.translate(invItem.x, invItem.y, invItem.z);
            poseStack.scale(1.0f - FluidRenderHelper.pixelFraction, 1.0f - FluidRenderHelper.pixelFraction, 1.0f - FluidRenderHelper.pixelFraction);
            if (!invItem.isBlockItem) {
                poseStack.translate(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            }
            invItem.stackState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(FoundryControllerBlockEntity be) {
        var mb = be.getMultiblockData();
        if (mb == null) {
            return new AABB(be.getBlockPos());
        }
        BlockPos c1 = mb.topCorners().getFirst();
        BlockPos c2 = mb.topCorners().getSecond();
        return new AABB(
                Math.min(c1.getX(), c2.getX()), c1.getY() - mb.height() + 1, Math.min(c1.getZ(), c2.getZ()),
                Math.max(c1.getX(), c2.getX()) + 1, c1.getY() + 1, Math.max(c1.getZ(), c2.getZ()) + 1
        );
    }

    @Override
    public boolean shouldRender(FoundryControllerBlockEntity blockEntity, Vec3 cameraPosition) {
        return true;
    }

    public static class FluidLayer
    {
        public final FluidStack stack;
        public final float yStart;
        public final float yEnd;
        public final float offsetY;

        public FluidLayer(FluidStack stack, float yStart, float yEnd, float offsetY) {
            this.stack = stack;
            this.yStart = yStart;
            this.yEnd = yEnd;
            this.offsetY = offsetY;
        }
    }

    public static class InvItem
    {
        public final ItemStackRenderState stackState = new ItemStackRenderState();
        public double x;
        public double y;
        public double z;
        public boolean isBlockItem;
    }

    public static class FoundryRenderState extends BlockEntityRenderState
    {
        public boolean render;
        public int[] xRange = new int[0];
        public int[] zRange = new int[0];
        public final List<FluidLayer> fluidLayers = new ArrayList<>();
        public final List<InvItem> invItems = new ArrayList<>();
    }
}

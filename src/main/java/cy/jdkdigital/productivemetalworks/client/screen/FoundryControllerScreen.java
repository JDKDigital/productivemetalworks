package cy.jdkdigital.productivemetalworks.client.screen;

import com.mojang.datafixers.util.Pair;
import cy.jdkdigital.productivelib.util.FluidContainerUtil;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.menu.FoundryControllerContainer;
import cy.jdkdigital.productivemetalworks.network.MoveFoundryFluidData;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import cy.jdkdigital.productivemetalworks.util.TickingSlotInventoryHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoundryControllerScreen extends AbstractContainerScreen<FoundryControllerContainer>
{
    private static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "textures/gui/container/foundry_controller.png");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private boolean isScrolling;
    private float scrollOffs;
    private int fuelTanks = 0;

    // Save fluids positions for tooltip and interaction
    // tank index, <offset, height>
    Map<Integer, Pair<Integer, Integer>> fluidPositions = new HashMap<>();

    public FoundryControllerScreen(FoundryControllerContainer container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
        this.imageWidth = 195;
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.blockEntity.getMultiblockData() != null) {
            this.menu.scrollTo(this.scrollOffs);
            this.fuelTanks = (int) this.menu.blockEntity.getMultiblockData().peripherals().stream().filter(blockPos -> this.menu.blockEntity.getLevel().getBlockState(blockPos).is(ModTags.Blocks.FOUNDRY_TANKS)).count();
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // Draw main screen
        guiGraphics.blit(GUI, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.getXSize(), this.getYSize());

        // Draw slots
        if (this.menu.blockEntity.getItemHandler() instanceof TickingSlotInventoryHandler itemHandler) {
            int rowIndex = this.menu.getRowIndexForScroll(this.scrollOffs);
            int slotsAfterScroll = itemHandler.getSlots() - (rowIndex * FoundryControllerContainer.COLUMNS);
            // calculate number of rows under the scroll fold
            int rows = this.menu.calculateRowCount(rowIndex);
            for (int row = 0; row < Math.min(3, rows); row++) {
                for (int i = 0; i < FoundryControllerContainer.COLUMNS; i++) {
                    int slot = (row * FoundryControllerContainer.COLUMNS) + (rowIndex * FoundryControllerContainer.COLUMNS) + i;
                    if (row * FoundryControllerContainer.COLUMNS + i < slotsAfterScroll && slot < itemHandler.getSlots() && this.menu.slots.get(slot).isActive()) {
                        int slotX = this.getGuiLeft() + 79 + (i * 18);
                        int slotY = this.getGuiTop() + 16 + (row * 18);
                        guiGraphics.blit(GUI, slotX, slotY, 195, 0, 18, 18);

                        var stack = itemHandler.getStackInSlot(slot);
                        if (!stack.isEmpty()) {
                            var ticker = itemHandler.getTicker(slot);
                            if (ticker.getSecond() != 0 && !ticker.getFirst().equals(ticker.getSecond())) {
                                int progress = (int) (18f - ((float)ticker.getFirst() / (float)ticker.getSecond()) * 18f);
                                guiGraphics.blit(GUI, slotX, slotY + (18-progress), 195, 36 - progress, 18, progress);
                            } else {
                                guiGraphics.blit(GUI, slotX, slotY, 195, 36, 18, 18);
                            }
                        }
                    }
                }
            }
        }

        // Draw scrollbar
        guiGraphics.blitSprite(SCROLLER_SPRITE, this.getGuiLeft() + 175, this.getGuiTop() + 17 + (int)(37f * this.scrollOffs), 12, 15);

        // Draw fuel tank
        if (!this.menu.blockEntity.fuel.isEmpty()) {
            FluidContainerUtil.renderFluidTank(guiGraphics, this, this.menu.blockEntity.fuel, this.fuelTanks * 4000, 57, 17, 16, 52);
        }

        // Draw fluid tank
        int tankCapacity = this.menu.blockEntity.fluidHandler.getCapacity();
        if (fluidPositions.size() != this.menu.blockEntity.fluidHandler.getTanks()) {
            fluidPositions.clear();
        }
        int nextFluidOffsetFraction = 0;
        for (int tank = 0; tank < this.menu.blockEntity.fluidHandler.getTanks(); tank++) {
            FluidStack fluidStack = this.menu.blockEntity.fluidHandler.getFluidInTank(tank);
            if (!fluidStack.isEmpty()) {
                int adjustedAmount = Math.max(fluidStack.getAmount(), tankCapacity/52);
                double fluidHeight = Math.floor(52d * ((double) adjustedAmount / (double) tankCapacity)) + (tank > 0 ? 1 : 0);
                fluidPositions.put(tank, Pair.of(nextFluidOffsetFraction, (int) fluidHeight));
                FluidContainerUtil.renderFluidTank(guiGraphics, this, fluidStack, adjustedAmount + (tank > 0 ? tankCapacity/52 : 0), tankCapacity, 8, 17, 42, 52, 0, 0, -1 * nextFluidOffsetFraction);
                nextFluidOffsetFraction += (int) fluidHeight;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        List<FormattedCharSequence> tooltipList = new ArrayList<>();
        if (insideFuelTank(mouseX, mouseY)) {
            if (!this.menu.blockEntity.fuel.isEmpty()) {
                tooltipList.add(Component.literal(this.menu.blockEntity.fuel.getAmount() + "mb " + Component.translatable(this.menu.blockEntity.fuel.getFluid().getFluidType().getDescriptionId()).getString()).getVisualOrderText());
            }
        }

        if (insideTank(mouseX, mouseY)) {
            int tank = getHoveredTank(mouseX, mouseY);
            if (tank >= 0) {
                // TODO formatted amount to make big numbers readable
                FluidStack fluidStack = this.menu.blockEntity.fluidHandler.getFluidInTank(tank);
                tooltipList.add(Component.literal(fluidStack.getAmount() + "mb " + Component.translatable(fluidStack.getFluid().getFluidType().getDescriptionId()).getString()).getVisualOrderText());
            }
        }
        if (!tooltipList.isEmpty()) {
            guiGraphics.renderTooltip(font, tooltipList, mouseX - getGuiLeft(), mouseY - getGuiTop());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (insideTank(mouseX, mouseY)) {
            int tank = getHoveredTank(mouseX, mouseY);
            PacketDistributor.sendToServer(new MoveFoundryFluidData(this.menu.blockEntity.getBlockPos(), tank));
            return true;
        }
        if (this.insideScrollbar(mouseX, mouseY)) {
            this.isScrolling = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isScrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isScrolling) {
            this.scrollOffs = ((float)mouseY - (float)this.getGuiTop() - 24.5f) / 37f;
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.menu.scrollTo(this.scrollOffs);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (insideScrollbar(mouseX, mouseY) || insideContainer(mouseX, mouseY)) {
            this.scrollOffs = this.menu.subtractInputFromScroll(this.scrollOffs, scrollY);
            this.menu.scrollTo(this.scrollOffs);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private int getHoveredTank(double mouseX, double mouseY) {
        if (insideTank(mouseX, mouseY)) {
            for (Map.Entry<Integer, Pair<Integer, Integer>> entry : fluidPositions.entrySet()) {
                Pair<Integer, Integer> o = entry.getValue();
                if (isHovering(8, 69 - o.getFirst() - o.getSecond(), 42, o.getSecond(), mouseX, mouseY)) {
                    return entry.getKey();
                }
            }
        }
        return -1;
    }

    private boolean insideContainer(double mouseX, double mouseY) {
        return isHovering(80, 17, 88, 52, mouseX, mouseY);
    }

    private boolean insideFuelTank(double mouseX, double mouseY) {
        return isHovering(57, 17, 16, 52, mouseX, mouseY);
    }

    private boolean insideTank(double mouseX, double mouseY) {
        return isHovering(8, 17, 42, 52, mouseX, mouseY);
    }

    protected boolean insideScrollbar(double mouseX, double mouseY) {
        return isHovering(175, 17, 12, 52, mouseX, mouseY);
    }
}

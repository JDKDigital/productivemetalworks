package cy.jdkdigital.productivemetalworks.client.screen;

import com.mojang.datafixers.util.Pair;
import cy.jdkdigital.productivelib.client.screen.AbstractUpgradeableContainerScreen;
import cy.jdkdigital.productivelib.util.FluidContainerUtil;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;
import cy.jdkdigital.productivemetalworks.common.menu.FoundryControllerContainer;
import cy.jdkdigital.productivemetalworks.network.MoveFoundryFluidData;
import cy.jdkdigital.productivemetalworks.recipe.ItemMeltingRecipe;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import cy.jdkdigital.productivemetalworks.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoundryControllerScreen extends AbstractUpgradeableContainerScreen<FoundryControllerContainer>
{
    private static final Identifier GUI = Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "textures/gui/container/foundry_controller.png");
    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    private boolean isScrolling;
    private float scrollOffs;
    private int fuelTanks = 0;

    // Save fluids positions for tooltip and interaction
    // tank index, <offset, height>
    Map<Integer, Pair<Integer, Integer>> fluidPositions = new HashMap<>();

    public FoundryControllerScreen(FoundryControllerContainer container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.getBlockEntity().getMultiblockData() != null) {
            this.menu.scrollTo(this.scrollOffs);
            this.fuelTanks = (int) this.menu.getBlockEntity().getMultiblockData().peripherals().stream().filter(blockPos -> this.menu.getBlockEntity().getLevel().getBlockState(blockPos).is(ModTags.Blocks.FOUNDRY_TANKS)).count();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);

        // Draw main screen
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.getLeftPos(), this.getTopPos(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // Draw slots
        if (this.menu.getBlockEntity().getItemHandler() instanceof TickingSlotInventoryHandler itemHandler) {
            int rowIndex = this.menu.getRowIndexForScroll(this.scrollOffs);
            int slotsAfterScroll = itemHandler.size() - (rowIndex * FoundryControllerContainer.COLUMNS);
            // calculate number of rows under the scroll fold
            int rows = this.menu.calculateRowCount(rowIndex);
            for (int row = 0; row < Math.min(3, rows); row++) {
                for (int i = 0; i < FoundryControllerContainer.COLUMNS; i++) {
                    int slot = (row * FoundryControllerContainer.COLUMNS) + (rowIndex * FoundryControllerContainer.COLUMNS) + i;
                    if (row * FoundryControllerContainer.COLUMNS + i < slotsAfterScroll && slot < itemHandler.size() && this.menu.slots.get(slot).isActive()) {
                        int slotX = this.getLeftPos() + 79 + (i * 18);
                        int slotY = this.getTopPos() + 16 + (row * 18);
                        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, slotX, slotY, 202.0F, 0.0F, 18, 18, 256, 256);

                        var stack = itemHandler.getStackInSlot(slot);
                        if (!stack.isEmpty()) {
                            var ticker = itemHandler.getTicker(slot);
                            if (ticker.getSecond() != 0 && !ticker.getFirst().equals(ticker.getSecond())) {
                                int progress = (int) (18f - ((float) ticker.getFirst() / (float) ticker.getSecond()) * 18f);
                                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, slotX, slotY + (18 - progress), 202.0F, (float) (36 - progress), 18, progress, 256, 256);
                            } else {
                                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, slotX, slotY, 202.0F, 36.0F, 18, 18, 256, 256);
                            }
                        }
                    }
                }
            }
        }

        // Draw scrollbar
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, this.getLeftPos() + 156, this.getTopPos() + 17 + (int) (37f * this.scrollOffs), 12, 15);

        switch (this.menu.getBlockEntity().getCoilType()) {
            case UNKNOWN, FLUID -> {
                // Draw fuel tank
                if (!this.menu.getBlockEntity().fuel.isEmpty()) {
                    FluidStack fuel = this.menu.getBlockEntity().fuel;
                    FluidContainerUtil.renderTiledFluid(guiGraphics, this, fuel, fuel.getAmount(), this.fuelTanks * 4000, 57, 17, 16, 52);
                }
                break;
            }
            case ENERGY -> {
                // Draw energy tank
                if (this.menu.getBlockEntity().getPowerMax() == 0) {
                    break;
                }

                // battery
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, getLeftPos() + 56, getTopPos() + 14, 238.0F, 0.0F, 18, 56, 256, 256);

                // energy
                float powerRatio = ((float) this.menu.getBlockEntity().getPower() / (float) this.menu.getBlockEntity().getPowerMax());
                int energyLevel = (int) ((52f * powerRatio) + 0.5f);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, getLeftPos() + 57, getTopPos() + 17 + 52 - energyLevel, 239.0F, 59.0F, 16, energyLevel, 256, 256);
            }
        }

        // Draw fluid tank
        int tankCapacity = this.menu.getBlockEntity().getFluidHandler().getCapacity();
        if (fluidPositions.size() != this.menu.getBlockEntity().getFluidHandler().getTanks()) {
            fluidPositions.clear();
        }
        int fluidCount = 0;
        for (int tank = 0; tank < this.menu.getBlockEntity().getFluidHandler().getTanks(); tank++) {
            if (!this.menu.getBlockEntity().getFluidHandler().getFluidInTank(tank).isEmpty()) {
                fluidCount++;
            }
        }
        int tankHeight = 52;
        int fluidMinHeight = 4;
        int fluidMinAmount = tankCapacity / tankHeight * fluidMinHeight;
        int fluidMaxAmount = tankCapacity - (fluidCount * fluidMinAmount - fluidMinAmount);

        int nextFluidOffset = 0;
        for (int tank = 0; tank < this.menu.getBlockEntity().fluidHandler.getTanks(); tank++) {
            FluidStack fluidStack = this.menu.getBlockEntity().fluidHandler.getFluidInTank(tank);
            if (!fluidStack.isEmpty()) {
                int adjustedAmount = Math.max(Math.min(fluidMaxAmount, fluidStack.getAmount()), fluidMinAmount);
                int fluidHeight = (int) Math.round(tankHeight * ((double) adjustedAmount / (double) tankCapacity));
                fluidPositions.put(tank, Pair.of(nextFluidOffset, fluidHeight));
                // Render the fluid's still sprite into this stacked sub-rect. renderTiledFluid tiles the
                // sprite at native size and scissor-clips the edges, so the 42px-wide tank keeps the
                // sprite's aspect (amount==capacity fills exactly fluidHeight at GUI-local y).
                int fluidYLocal = 17 + 52 - fluidHeight - nextFluidOffset;
                FluidContainerUtil.renderTiledFluid(guiGraphics, this, fluidStack, 1, 1, 8, fluidYLocal, 42, fluidHeight);
                nextFluidOffset += fluidHeight;
            }
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltips = super.getTooltipFromContainerItem(stack);

        if (!stack.isEmpty()) {
            FoundryControllerBlockEntity.IMelterProcessor melter = switch (this.menu.getBlockEntity().getCoilType()) {
                case UNKNOWN -> null;
                case CoilType.FLUID -> new FoundryControllerBlockEntity.LiquidMelter();
                case CoilType.ENERGY -> new FoundryControllerBlockEntity.EnergyMelter();
            };
            if (melter != null) {
                FuelMap fuelData = melter.getFoundryFuel(this.menu.getBlockEntity().getLevel(), this.menu.getBlockEntity()).getFuelData();
                if (fuelData != null) {
                    RecipeHolder<ItemMeltingRecipe> recipe = RecipeHelper.getItemMeltingRecipe(Minecraft.getInstance().level, stack, fuelData);
                    if (recipe != null) {
                        int speedModifier = this.menu.getBlockEntity().getSpeedModifier();
                        // Sum total fluid amount that would be melted
                        int totalProducedFluid = recipe.value().result.stream().map(FluidStackTemplate::getAmount).reduce(Integer::sum).orElse(0);

                        // Look at the fuel required for this recipe melt
                        int requiredFuel = (int) (totalProducedFluid * fuelData.consumption() * speedModifier);
                        boolean hasEnough = requiredFuel <= (this.menu.getBlockEntity().getCoilType().equals(CoilType.ENERGY) ? this.menu.getBlockEntity().getPower() : this.menu.getBlockEntity().getFuel().getAmount());
                        tooltips.add(tooltips.size() - 1, Component.translatable("gui.productivemetalworks.required_fuel", requiredFuel + (this.menu.getBlockEntity().getCoilType().equals(CoilType.ENERGY) ? " FE" : " mb")).withStyle(hasEnough ? ChatFormatting.GREEN : ChatFormatting.RED));
                    }
                }
            }
        }

        return tooltips;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);

        List<FormattedCharSequence> tooltipList = new ArrayList<>();
        if (insideFuelTank(mouseX, mouseY)) {
            FoundryControllerBlockEntity.IMelterProcessor melter = switch (this.menu.getBlockEntity().getCoilType()) {
                case UNKNOWN -> null;
                case CoilType.FLUID -> new FoundryControllerBlockEntity.LiquidMelter();
                case CoilType.ENERGY -> new FoundryControllerBlockEntity.EnergyMelter();
            };
            switch (this.menu.getBlockEntity().getCoilType()) {
                case FLUID -> {
                    if (!this.menu.getBlockEntity().fuel.isEmpty()) {
                        tooltipList.add(Component.literal(this.menu.getBlockEntity().fuel.getAmount() + "mb " + Component.translatable(this.menu.getBlockEntity().fuel.getFluid().getFluidType().getDescriptionId()).getString()).getVisualOrderText());
                        if (melter != null) {
                            var fuelData = melter.getFoundryFuel(this.menu.getBlockEntity().getLevel(), this.menu.getBlockEntity()).getFuelData();
                            if (fuelData != null) {
                                tooltipList.add(Component.translatable("gui.productivemetalworks.temperature", fuelData.temperature()).getVisualOrderText());
                            }
                        }
                    }
                }
                case ENERGY -> {
                    tooltipList.add(Component.literal(this.menu.getBlockEntity().getPower() + " FE").getVisualOrderText());
                    if (melter != null) {
                        var fuelData = melter.getFoundryFuel(this.menu.getBlockEntity().getLevel(), this.menu.getBlockEntity()).getFuelData();
                        if (fuelData != null) {
                            tooltipList.add(Component.translatable("gui.productivemetalworks.temperature", fuelData.temperature()).getVisualOrderText());
                        }
                    }
                }
            }
        }

        if (insideTank(mouseX, mouseY)) {
            int tank = getHoveredTank(mouseX, mouseY);
            if (tank >= 0) {
                FluidStack fluidStack = this.menu.getBlockEntity().getFluidHandler().getFluidInTank(tank);
                tooltipList.add(Component.literal(fluidStack.getAmount() + "mb " + Component.translatable(fluidStack.getFluid().getFluidType().getDescriptionId()).getString()).getVisualOrderText());
                tooltipList.addAll(FluidHelper.formatTooltip(fluidStack).stream().map(Component::getVisualOrderText).toList());
            }
        }
        if (!tooltipList.isEmpty()) {
            // setTooltipForNextFrame positions relative to the cursor in absolute screen coords; subtracting
            // the GUI origin offset the tooltip away from the mouse by an amount that scaled with window size.
            guiGraphics.setTooltipForNextFrame(font, tooltipList, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (insideTank(mouseX, mouseY)) {
            int tank = getHoveredTank(mouseX, mouseY);
            ClientPacketDistributor.sendToServer(new MoveFoundryFluidData(this.menu.getBlockEntity().getBlockPos(), tank));
            return true;
        }
        if (this.insideScrollbar(mouseX, mouseY)) {
            this.isScrolling = true;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            this.isScrolling = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.isScrolling) {
            this.scrollOffs = ((float) event.y() - (float) this.getTopPos() - 24.5f) / 37f;
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.menu.scrollTo(this.scrollOffs);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
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
        return isHovering(156, 17, 12, 52, mouseX, mouseY);
    }
}

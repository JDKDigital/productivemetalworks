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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoundryControllerScreen extends AbstractUpgradeableContainerScreen<FoundryControllerContainer>
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
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // Draw main screen
        guiGraphics.blit(GUI, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.getXSize(), this.getYSize());

        // Draw slots
        if (this.menu.getBlockEntity().getItemHandler() instanceof TickingSlotInventoryHandler itemHandler) {
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
                        guiGraphics.blit(GUI, slotX, slotY, 202, 0, 18, 18);

                        var stack = itemHandler.getStackInSlot(slot);
                        if (!stack.isEmpty()) {
                            var ticker = itemHandler.getTicker(slot);
                            if (ticker.getSecond() != 0 && !ticker.getFirst().equals(ticker.getSecond())) {
                                int progress = (int) (18f - ((float) ticker.getFirst() / (float) ticker.getSecond()) * 18f);
                                guiGraphics.blit(GUI, slotX, slotY + (18 - progress), 202, 36 - progress, 18, progress);
                            } else {
                                guiGraphics.blit(GUI, slotX, slotY, 202, 36, 18, 18);
                            }
                        }
                    }
                }
            }
        }

        // Draw scrollbar
        guiGraphics.blitSprite(SCROLLER_SPRITE, this.getGuiLeft() + 156, this.getGuiTop() + 17 + (int) (37f * this.scrollOffs), 12, 15);

        switch (this.menu.getBlockEntity().getCoilType()) {
            case UNKNOWN, FLUID -> {
                // Draw fuel tank
                if (!this.menu.getBlockEntity().fuel.isEmpty()) {
                    FluidContainerUtil.renderFluidTank(guiGraphics, this, this.menu.getBlockEntity().fuel, this.fuelTanks * 4000, 57, 17, 16, 52);
                }
                break;
            }
            case ENERGY -> {
                // Draw energy tank
                if (this.menu.getBlockEntity().getPowerMax() == 0) {
                    break;
                }

                // battery
                guiGraphics.blit(GUI, getGuiLeft() + 56, getGuiTop() + 14, 238, 0, 18, 56);

                // energy
                float powerRatio = ((float) this.menu.getBlockEntity().getPower() / (float) this.menu.getBlockEntity().getPowerMax());
                int energyLevel = (int) ((52f * powerRatio) + 0.5f);
                guiGraphics.blit(GUI, getGuiLeft() + 57, getGuiTop() + 17 + 52 - energyLevel, 239,  59, 16, energyLevel);
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
                double fluidHeight = Math.round(tankHeight * ((double) adjustedAmount / (double) tankCapacity));
                fluidPositions.put(tank, Pair.of(nextFluidOffset, (int) fluidHeight));
                FluidContainerUtil.renderTiledFluid(guiGraphics, this, fluidStack, 8, 17 + 52 - (int) fluidHeight - nextFluidOffset, 42, (int) fluidHeight, 0);
                nextFluidOffset += (int) fluidHeight;
            }
        }
    }

    @Override
    protected @NotNull List<Component> getTooltipFromContainerItem(ItemStack stack) {
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
                        int totalProducedFluid = recipe.value().result.stream().map(FluidStack::getAmount).reduce(Integer::sum).orElse(0);

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
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

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
            guiGraphics.renderTooltip(font, tooltipList, mouseX - getGuiLeft(), mouseY - getGuiTop());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (insideTank(mouseX, mouseY)) {
            int tank = getHoveredTank(mouseX, mouseY);
            PacketDistributor.sendToServer(new MoveFoundryFluidData(this.menu.getBlockEntity().getBlockPos(), tank));
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
            this.scrollOffs = ((float) mouseY - (float) this.getGuiTop() - 24.5f) / 37f;
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
        return isHovering(156, 17, 12, 52, mouseX, mouseY);
    }
}

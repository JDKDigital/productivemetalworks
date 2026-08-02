package cy.jdkdigital.productivemetalworks.common.menu;

import cy.jdkdigital.productivelib.container.AbstractContainer;
import cy.jdkdigital.productivelib.container.ManualSlotItemHandler;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;

public class FoundryControllerContainer extends AbstractContainer<FoundryControllerBlockEntity>
{
    public static int COLUMNS = 4;

    // Menu-slot index range [0, mainSlotEnd) backed by the controller's main item handler.
    // ManualSlotItemHandler no longer exposes its backing handler in 26.1, so the scroll logic
    // identifies the scrollable rows by this range instead.
    private int mainSlotEnd = 0;

    public FoundryControllerContainer(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, data));
    }

    public FoundryControllerContainer(final int windowId, final Inventory playerInventory, final FoundryControllerBlockEntity blockEntity) {
        super(MetalworksRegistrator.FOUNDRY_CONTROLLER_CONTAINER.get(), blockEntity, windowId);

        // Energy
//        addDataSlot(new DataSlot()
//        {
//            @Override
//            public int get() {
//                return blockEntity.energyHandler.getEnergyStored();
//            }
//
//            @Override
//            public void set(int value) {
//                if (blockEntity.energyHandler.getEnergyStored() > 0) {
//                    blockEntity.energyHandler.extractEnergy(blockEntity.energyHandler.getEnergyStored(), false);
//                }
//                if (value > 0) {
//                    blockEntity.energyHandler.receiveEnergy(value, false, true);
//                }
//            }
//        });

        // Fluid fuel
        addDataSlots(new ContainerData()
        {
            @Override
            public int get(int i) {
                var fuel = blockEntity.getFuel();
                return i == 0 ?
                        BuiltInRegistries.FLUID.getId(fuel.getFluid()) :
                        fuel.getAmount();
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0:
                        blockEntity.fuel = new FluidStack(BuiltInRegistries.FLUID.byId(value), 1);
                    case 1:
                        blockEntity.fuel.setAmount(value);
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        // Fluid types
        addDataSlots(new ContainerData()
        {
            @Override
            public int get(int i) {
                var fluid = blockEntity.fluidHandler.getFluidInTank(i);
                return !fluid.isEmpty() ? BuiltInRegistries.FLUID.getId(fluid.getFluid()) : 0;
            }

            @Override
            public void set(int i, int value) {
                FluidStack fluid = blockEntity.fluidHandler.getFluidInTank(i);
                if (fluid.isEmpty()) {
                    blockEntity.fluidHandler.fill(new FluidStack(BuiltInRegistries.FLUID.byId(value), 1), true);
                }
            }

            @Override
            public int getCount() {
                return 20;
            }
        });

        // Fluid amounts
        addDataSlots(new ContainerData()
        {
            @Override
            public int get(int i) {
                return blockEntity.fluidHandler.getFluidInTank(i).getAmount();
            }

            @Override
            public void set(int i, int value) {
                blockEntity.fluidHandler.getFluidInTank(i).setAmount(value);
            }

            @Override
            public int getCount() {
                return 20;
            }
        });

        int rowCount = calculateRowCount(0);
        int leftover = this.getBlockEntity().getItemHandler().size()%COLUMNS;
        if (rowCount > 0) {
            addSlotBox(this.getBlockEntity().getItemHandler(), 0, 80, 17, COLUMNS, 18, rowCount, 18);
        }
        addSlotRange(this.getBlockEntity().getItemHandler(), rowCount * COLUMNS, 80, 17 + rowCount * 18, leftover, 18);
        this.mainSlotEnd = this.slots.size();

        addSlotBox(this.getBlockEntity().getUpgradeHandler(), 0, 178, 8, 1, 18, 4, 18);

        layoutPlayerInventorySlots(playerInventory, 0, 8, 84);
    }

    private static FoundryControllerBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null!");
        Objects.requireNonNull(data, "data cannot be null!");
        if (playerInventory.player.level().getBlockEntity(data.readBlockPos()) instanceof FoundryControllerBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Block entity is not correct for Foundry Controller Container!");
    }

    public float subtractInputFromScroll(float scrollOffs, double input) {
        return Mth.clamp(scrollOffs - (float)(input / (double)this.calculateRowCount(0)), 0.0F, 1.0F);
    }

    public void scrollTo(float scrollOffs) {
        int offsetRow = this.getRowIndexForScroll(scrollOffs);

        this.slots.forEach(slot -> {
            if (slot instanceof ManualSlotItemHandler mSlot && slot.index < this.mainSlotEnd) {
                // disable slots above and below the shown rows
                if (slot.index < offsetRow * FoundryControllerContainer.COLUMNS || slot.index > offsetRow * FoundryControllerContainer.COLUMNS + 11) {
                    mSlot.disable();
                } else {
                    mSlot.enable();
                }
                slot.y = Math.floorDiv(slot.index, FoundryControllerContainer.COLUMNS) * 18 - offsetRow * 18 + 17;
            }
        });
    }

    public int calculateRowCount(int offset) {
        return Mth.positiveCeilDiv(this.getBlockEntity().getItemHandler().size() - (offset * FoundryControllerContainer.COLUMNS), FoundryControllerContainer.COLUMNS);
    }

    public int getRowIndexForScroll(float scrollOffs) {
        return Math.max((int)((scrollOffs * (float)this.calculateRowCount(3)) + 0.5), 0);
    }
}

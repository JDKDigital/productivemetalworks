package cy.jdkdigital.productivemetalworks.common.menu;

import cy.jdkdigital.productivelib.container.AbstractContainer;
import cy.jdkdigital.productivelib.container.ManualSlotItemHandler;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class FoundryControllerContainer extends AbstractContainer
{
    public static int COLUMNS = 5;
    private final Inventory playerInventory;
    public final FoundryControllerBlockEntity blockEntity;

//    static final SimpleContainer CONTAINER = new SimpleContainer(15);

    public FoundryControllerContainer(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, data));
    }

    public FoundryControllerContainer(final int windowId, final Inventory playerInventory, final FoundryControllerBlockEntity blockEntity) {
        super(MetalworksRegistrator.FOUNDRY_CONTROLLER_CONTAINER.get(), windowId);
        this.playerInventory = playerInventory;
        this.blockEntity = blockEntity;

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
                    blockEntity.fluidHandler.fill(new FluidStack(BuiltInRegistries.FLUID.byId(value), 1), IFluidHandler.FluidAction.EXECUTE);
                }
            }

            @Override
            public int getCount() {
                return 20;
            }
        });
        // Fluid amount
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

        int rowCount = calculateRowCount(1);
        int leftover = this.blockEntity.getItemHandler().getSlots()%COLUMNS;
        if (rowCount > 0) {
            addSlotBox(this.blockEntity.getItemHandler(), 0, 80, 17, COLUMNS, 18, rowCount, 18);
        }
        addSlotRange(this.blockEntity.getItemHandler(), rowCount * 5, 80, 17 + rowCount * 18, leftover, 18);

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

    @Override
    public boolean stillValid(@Nonnull final Player player) {
        return blockEntity.getBlockState().getValue(BlockStateProperties.ATTACHED) && blockEntity.getMultiblockData() != null && player.distanceToSqr((double)blockEntity.getBlockPos().getX() + 0.5, (double)blockEntity.getBlockPos().getY() + 0.5, (double)blockEntity.getBlockPos().getZ() + 0.5) <= 64.0 && !blockEntity.isRemoved();
    }

    @Override
    protected BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public float subtractInputFromScroll(float scrollOffs, double input) {
        return Mth.clamp(scrollOffs - (float)(input / (double)this.calculateRowCount(0)), 0.0F, 1.0F);
    }

    public void scrollTo(float scrollOffs) {
        int offsetRow = this.getRowIndexForScroll(scrollOffs);

        this.slots.forEach(slot -> {
            if (slot instanceof ManualSlotItemHandler mSlot && mSlot.getItemHandler().equals(this.blockEntity.getItemHandler())) {
                // disable slots above and below the shown rows
                if (slot.index < offsetRow * FoundryControllerContainer.COLUMNS || slot.index > offsetRow * FoundryControllerContainer.COLUMNS + 14) {
                    mSlot.disable();
                } else {
                    mSlot.enable();
                }
                slot.y = Math.floorDiv(slot.index, FoundryControllerContainer.COLUMNS) * 18 - offsetRow * 18 + 17;
            }
        });
    }

    public int calculateRowCount(int offset) {
        return Mth.positiveCeilDiv(this.blockEntity.getItemHandler().getSlots() - (offset * FoundryControllerContainer.COLUMNS), FoundryControllerContainer.COLUMNS);
    }

    public int getRowIndexForScroll(float scrollOffs) {
        return Math.max((int)((scrollOffs * (float)this.calculateRowCount(3)) + 0.5), 0);
    }
}

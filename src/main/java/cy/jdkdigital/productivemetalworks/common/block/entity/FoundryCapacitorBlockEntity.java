package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.CapabilityBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.FluidTankBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.IMultiBlockPeripheralBlockEntity;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FoundryCapacitorBlockEntity extends CapabilityBlockEntity implements IMultiBlockPeripheralBlockEntity
{
    private BlockPos controllerPosition;

    public EnergyStorage energyHandler = new EnergyStorage(40000) {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            var receivedEnergy = super.receiveEnergy(toReceive, simulate);
            if (receivedEnergy > 0 && level instanceof ServerLevel) {
                sync(level);
            }
            return receivedEnergy;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            var extractedEnergy = super.extractEnergy(toExtract, simulate);
            if (extractedEnergy > 0 && level instanceof ServerLevel) {
                sync(level);
            }
            return extractedEnergy;
        }
    };

    public FoundryCapacitorBlockEntity(BlockPos pos, BlockState blockState) {
        super(MetalworksRegistrator.FOUNDRY_CAPACITOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public EnergyStorage getEnergyHandler() {
        return energyHandler;
    }

    @Override
    public void setMultiblockController(BlockPos pos) {
        this.controllerPosition = pos;
    }

    @Override
    public BlockPos getMultiblockController() {
        return this.controllerPosition;
    }

    @Override
    public void savePacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.savePacketNBT(tag, provider);

        if (this.controllerPosition != null) {
            tag.putLong("controller", this.controllerPosition.asLong());
        }
    }

    @Override
    public void loadPacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadPacketNBT(tag, provider);

        if (tag.contains("controller")) {
            this.controllerPosition = BlockPos.of(tag.getLong("controller"));
        }
    }

    public void sync(Level level) {
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, FoundryCapacitorBlockEntity capacitorBlockEntity) {
        // share energy among other capacitors in the multiblock
        if (capacitorBlockEntity.getMultiblockController() != null && level.getBlockEntity(capacitorBlockEntity.getMultiblockController()) instanceof FoundryControllerBlockEntity controllerBlockEntity) {
            var mb = controllerBlockEntity.getMultiblockData();
            if (mb != null) {
                mb.peripherals().forEach(pos -> {
                    if (!pos.equals(capacitorBlockEntity.getBlockPos()) && level.getBlockEntity(pos) instanceof CapabilityBlockEntity otherCapacitor) {
                        int energyDiff = capacitorBlockEntity.getEnergyHandler().getEnergyStored() - otherCapacitor.getEnergyHandler().getEnergyStored();
                        if (energyDiff > 1) {
                            int transferred = otherCapacitor.getEnergyHandler().receiveEnergy(energyDiff/2, false);
                            capacitorBlockEntity.getEnergyHandler().extractEnergy(transferred, false);
                        }
                    }
                });
            }
        }
    }

//    @Override
//    protected void applyImplicitComponents(DataComponentInput componentInput) {
//        super.applyImplicitComponents(componentInput);
//        ImmutableFluidStack fluid = componentInput.getOrDefault(MetalworksRegistrator.FLUID_STACK.get(), ImmutableFluidStack.EMPTY);
//        if (!fluid.fluid().isEmpty()) {
//            this.getEnergyHandler().setFluid(fluid.fluid().copy());
//        }
//    }
//
//    @Override
//    protected void collectImplicitComponents(DataComponentMap.Builder components) {
//        super.collectImplicitComponents(components);
//        if (!this.getFluidHandler().getFluid().isEmpty()) {
//            components.set(MetalworksRegistrator.FLUID_STACK.get(), new ImmutableFluidStack(this.getFluidHandler().getFluid().copy()));
//        }
//    }
}

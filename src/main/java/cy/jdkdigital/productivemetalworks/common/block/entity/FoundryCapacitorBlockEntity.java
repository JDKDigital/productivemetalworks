package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.CapabilityBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.IMultiBlockPeripheralBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;

public class FoundryCapacitorBlockEntity extends CapabilityBlockEntity implements IMultiBlockPeripheralBlockEntity
{
    private BlockPos controllerPosition;

    public EnergyStorage energyHandler = new EnergyStorage(10000);

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

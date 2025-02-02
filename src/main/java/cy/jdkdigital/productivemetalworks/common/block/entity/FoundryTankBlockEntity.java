package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.FluidTankBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.IMultiBlockPeripheralBlockEntity;
import cy.jdkdigital.productivelib.util.ImmutableFluidStack;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FoundryTankBlockEntity extends FluidTankBlockEntity implements IMultiBlockPeripheralBlockEntity
{
    private BlockPos controllerPosition;

    public FluidTank fluidHandler = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            if (FoundryTankBlockEntity.this.level instanceof ServerLevel serverLevel) {
                if (FoundryTankBlockEntity.this.getMultiblockController() != null && serverLevel.getBlockEntity(FoundryTankBlockEntity.this.getMultiblockController()) instanceof FoundryControllerBlockEntity foundry) {
                    foundry.sync(serverLevel);
                }
                FoundryTankBlockEntity.this.sync(serverLevel);
            }
        }
    };

    public FoundryTankBlockEntity(BlockPos pos, BlockState blockState) {
        super(MetalworksRegistrator.FOUNDRY_TANK_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void tickFluidTank(Level level, BlockPos blockPos, BlockState blockState, FluidTankBlockEntity fluidTankBlockEntity) {
        if (level.getBlockEntity(blockPos.below()) instanceof FoundryTankBlockEntity belowTank && belowTank.getFluidHandler().getSpace() > 0) {
            FluidUtil.tryFluidTransfer(belowTank.getFluidHandler(), fluidTankBlockEntity.getFluidHandler(), 1000, true);
        }
    }

    @Override
    public FluidTank getFluidHandler() {
        return fluidHandler;
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

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, FoundryTankBlockEntity tankBlockEntity) {
        FluidTankBlockEntity.tick(level, blockPos, blockState, tankBlockEntity);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        ImmutableFluidStack fluid = componentInput.getOrDefault(MetalworksRegistrator.FLUID_STACK.get(), ImmutableFluidStack.EMPTY);
        if (!fluid.fluid().isEmpty()) {
            this.getFluidHandler().setFluid(fluid.fluid());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!this.getFluidHandler().getFluid().isEmpty()) {
            components.set(MetalworksRegistrator.FLUID_STACK.get(), new ImmutableFluidStack(this.getFluidHandler().getFluid()));
        }
    }
}

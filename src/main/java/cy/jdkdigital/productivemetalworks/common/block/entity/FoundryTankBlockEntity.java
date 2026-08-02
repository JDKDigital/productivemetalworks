package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.FluidTankBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.IMultiBlockPeripheralBlockEntity;
import cy.jdkdigital.productivelib.util.ImmutableFluidStack;
import cy.jdkdigital.productivemetalworks.Config;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.ModFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;

public class FoundryTankBlockEntity extends FluidTankBlockEntity implements IMultiBlockPeripheralBlockEntity
{
    private BlockPos controllerPosition;

    public ModFluidTank fluidHandler = new ModFluidTank(4000) {
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
            // drain fluid downwards into the tank below
            FluidStack drained = this.fluidHandler.drain(4000, false);
            if (!drained.isEmpty()) {
                int fillable = belowTank.getFluidHandler().fill(drained, false);
                if (fillable > 0) {
                    FluidStack moved = this.fluidHandler.drain(fillable, true);
                    belowTank.getFluidHandler().fill(moved, true);
                }
            }
        }
    }

    @Override
    public int tankTickRate() {
        return Config.foundryTankTickRate;
    }

    @Override
    public ModFluidTank getFluidHandler() {
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
    public void savePacketNBT(ValueOutput output) {
        super.savePacketNBT(output);

        if (this.controllerPosition != null) {
            output.putLong("controller", this.controllerPosition.asLong());
        }
    }

    @Override
    public void loadPacketNBT(ValueInput input) {
        super.loadPacketNBT(input);

        input.getLong("controller").ifPresent(l -> this.controllerPosition = BlockPos.of(l));
    }

    public void sync(Level level) {
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, FoundryTankBlockEntity tankBlockEntity) {
        FluidTankBlockEntity.tick(level, blockPos, blockState, tankBlockEntity);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        super.applyImplicitComponents(componentInput);
        ImmutableFluidStack fluid = componentInput.getOrDefault(MetalworksRegistrator.FLUID_STACK.get(), ImmutableFluidStack.EMPTY);
        if (!fluid.fluid().isEmpty()) {
            this.getFluidHandler().setFluid(fluid.fluid().copy());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!this.getFluidHandler().getFluid().isEmpty()) {
            components.set(MetalworksRegistrator.FLUID_STACK.get(), new ImmutableFluidStack(this.getFluidHandler().getFluid().copy()));
        }
    }
}

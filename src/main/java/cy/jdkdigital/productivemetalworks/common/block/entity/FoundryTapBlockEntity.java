package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.AbstractBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class FoundryTapBlockEntity extends AbstractBlockEntity
{
    public boolean isActive = false;
    public int fluidId = 0;
    private int activationCounter = 0;
    public FoundryTapBlockEntity(BlockPos pos, BlockState blockState) {
        super(MetalworksRegistrator.FOUNDRY_TAP_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, FoundryTapBlockEntity blockEntity) {
        // Every 10 ticks check for redstone signal and turn on
        if (!blockEntity.isActive && ++blockEntity.activationCounter%10 == 0 && level.hasNeighborSignal(blockPos)) {
            blockEntity.setActive(true);
            blockEntity.activationCounter = 0;
        }

        // Transfer fluid when active from connected fluid container to fluid container below
        if (blockEntity.isActive) {
            var direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            ResourceHandler<FluidResource> drainSource = level.getCapability(Capabilities.Fluid.BLOCK, blockPos.relative(direction.getOpposite()), direction);
            ResourceHandler<FluidResource> destination = level.getCapability(Capabilities.Fluid.BLOCK, blockPos.below(), Direction.UP);
            int moved = 0;
            if (drainSource != null && destination != null) {
                try (Transaction tx = Transaction.openRoot()) {
                    moved = ResourceHandlerUtil.move(drainSource, destination, resource -> true, 10, tx);
                    if (moved > 0) {
                        tx.commit();
                    }
                }
            }
            if (moved > 0) {
                int fId = BuiltInRegistries.FLUID.getId(destination.getResource(0).getFluid());
                if (blockEntity.fluidId != fId) {
                    blockEntity.fluidId = fId;
                    blockEntity.sync(level);
                }
            } else {
                blockEntity.isActive = false;
                blockEntity.fluidId = 0;
                blockEntity.sync(level);
            }
        }
    }

    public void setActive(boolean isActive) {
        var dir = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (level != null && level.getCapability(Capabilities.Fluid.BLOCK, getBlockPos().relative(dir.getOpposite()), dir) != null) {
            this.isActive = isActive;
            if (!isActive) {
                this.fluidId = 0;
            }
            if (level != null) {
                this.sync(level);
            }
        }
    }

    @Override
    public void loadPacketNBT(ValueInput input) {
        super.loadPacketNBT(input);

        this.fluidId = input.getIntOr("fluidId", 0);
        this.isActive = input.getBooleanOr("isActive", false);
    }

    @Override
    public void savePacketNBT(ValueOutput output) {
        super.savePacketNBT(output);

        output.putInt("fluidId", this.fluidId);
        output.putBoolean("isActive", this.isActive);
    }

    public void sync(Level level) {
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}

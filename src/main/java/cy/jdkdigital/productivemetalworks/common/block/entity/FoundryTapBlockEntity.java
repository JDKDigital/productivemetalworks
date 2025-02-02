package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.AbstractBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FoundryTapBlockEntity extends AbstractBlockEntity
{
    public boolean isActive = false;
    public int fluidId = 0;
    public FoundryTapBlockEntity(BlockPos pos, BlockState blockState) {
        super(MetalworksRegistrator.FOUNDRY_TAP_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, FoundryTapBlockEntity blockEntity) {
        // Transfer fluid when active from connected fluid container to fluid container below
        if (blockEntity.isActive) {
            var direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            var source = level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos.relative(direction.getOpposite()), direction);
            var destination = level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos.below(), Direction.UP);
            if (source != null && destination != null && !FluidUtil.tryFluidTransfer(destination, source, 10, false).isEmpty()) {
                FluidUtil.tryFluidTransfer(destination, source, 10, true);
                int fId = BuiltInRegistries.FLUID.getId(destination.getFluidInTank(0).getFluid());
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

    public void toggleActive() {
        var dir = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (level != null && level.getCapability(Capabilities.FluidHandler.BLOCK, getBlockPos().relative(dir.getOpposite()), dir) != null) {
            this.isActive = !this.isActive;
            if (!this.isActive) {
                this.fluidId = 0;
            }
            if (level != null) {
                this.sync(level);
            }
        }
    }

    @Override
    public void loadPacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadPacketNBT(tag, provider);

        if (tag.contains("fluidId")) {
            this.fluidId = tag.getInt("fluidId");
        }
        this.isActive = tag.contains("isActive") && tag.getBoolean("isActive");
    }

    @Override
    public void savePacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.savePacketNBT(tag, provider);

        tag.putInt("fluidId", this.fluidId);
        tag.putBoolean("isActive", this.isActive);
    }

    public void sync(Level level) {
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}

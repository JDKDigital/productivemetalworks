package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.CapabilityBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.IMultiBlockPeripheralBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class FoundryCapacitorBlockEntity extends CapabilityBlockEntity implements IMultiBlockPeripheralBlockEntity
{
    private BlockPos controllerPosition;
    private int tickCounter = 0;

    public SimpleEnergyHandler energyHandler = new SimpleEnergyHandler(40000, 40000, 40000) {
        @Override
        protected void onEnergyChanged(int previousAmount) {
            super.onEnergyChanged(previousAmount);
            if (level instanceof ServerLevel) {
                sync(level);
            }
        }
    };

    public FoundryCapacitorBlockEntity(BlockPos pos, BlockState blockState) {
        super(MetalworksRegistrator.FOUNDRY_CAPACITOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public EnergyHandler getEnergyHandler() {
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

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, FoundryCapacitorBlockEntity capacitorBlockEntity) {
        // share energy among other capacitors in the multiblock
        if (++capacitorBlockEntity.tickCounter%5 == 0 && capacitorBlockEntity.getMultiblockController() != null && level.getBlockEntity(capacitorBlockEntity.getMultiblockController()) instanceof FoundryControllerBlockEntity controllerBlockEntity) {
            capacitorBlockEntity.tickCounter = 0;
            var mb = controllerBlockEntity.getMultiblockData();
            if (mb != null) {
                mb.peripherals().forEach(pos -> {
                    if (!pos.equals(capacitorBlockEntity.getBlockPos()) && level.getBlockEntity(pos) instanceof FoundryCapacitorBlockEntity otherCapacitor) {
                        int energyDiff = capacitorBlockEntity.energyHandler.getAmountAsInt() - otherCapacitor.energyHandler.getAmountAsInt();
                        if (energyDiff > 0) {
                            try (Transaction tx = Transaction.openRoot()) {
                                int transferred = otherCapacitor.energyHandler.insert((int)Math.ceil(energyDiff/2d), tx);
                                capacitorBlockEntity.energyHandler.extract(transferred, tx);
                                tx.commit();
                            }
                        }
                    }
                });
            }
        }
    }
}

package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.AbstractBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.IMultiBlockPeripheralBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FoundryDrainBlockEntity extends AbstractBlockEntity implements IMultiBlockPeripheralBlockEntity
{
    private BlockPos controllerPosition;

    public FoundryDrainBlockEntity(BlockPos pos, BlockState blockState) {
        super(MetalworksRegistrator.FOUNDRY_DRAIN_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void setMultiblockController(BlockPos pos) {
        this.controllerPosition = pos;
        this.invalidateCapabilities();
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
}

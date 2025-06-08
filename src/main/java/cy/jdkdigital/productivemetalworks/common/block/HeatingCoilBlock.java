package cy.jdkdigital.productivemetalworks.common.block;

import cy.jdkdigital.productivemetalworks.util.CoilType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HeatingCoilBlock extends Block implements IHeatingCoilBlock
{
    private final CoilType coilType;

    public HeatingCoilBlock(Properties properties, CoilType coilType) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.ATTACHED, false));
        this.coilType = coilType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(BlockStateProperties.ATTACHED);
    }

    @Override
    public CoilType getCoilType() {
        return coilType;
    }
}

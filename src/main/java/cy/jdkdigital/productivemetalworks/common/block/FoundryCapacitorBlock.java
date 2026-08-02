package cy.jdkdigital.productivemetalworks.common.block;

import com.mojang.serialization.MapCodec;
import cy.jdkdigital.productivelib.common.block.IMultiBlockPeripheral;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryCapacitorBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class FoundryCapacitorBlock extends BaseEntityBlock implements IMultiBlockPeripheral
{
    public static final MapCodec<FoundryCapacitorBlock> CODEC = simpleCodec(FoundryCapacitorBlock::new);

    public FoundryCapacitorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryCapacitorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, MetalworksRegistrator.FOUNDRY_CAPACITOR_BLOCK_ENTITY.get(), FoundryCapacitorBlockEntity::serverTick);
    }

    // PORT-TODO (26.1): Block#appendHoverText removed — reinstate the stored-fluid tooltip via a
    //  custom BlockItem subclass if this block ever stores fluid components.
}

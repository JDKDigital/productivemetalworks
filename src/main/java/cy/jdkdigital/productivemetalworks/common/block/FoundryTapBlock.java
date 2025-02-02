package cy.jdkdigital.productivemetalworks.common.block;

import com.mojang.serialization.MapCodec;
import cy.jdkdigital.productivelib.common.block.IMultiBlockPeripheral;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTapBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FoundryTapBlock extends BaseEntityBlock implements IMultiBlockPeripheral
{
    public static final MapCodec<FoundryTapBlock> CODEC = simpleCodec(FoundryTapBlock::new);

    Map<Direction, VoxelShape> SHAPES = new HashMap()
    {{
        put(Direction.WEST, Shapes.join(Shapes.join(Shapes.empty(), Shapes.box(0.375, 0.375, 0.375, 1, 0.625, 0.625), BooleanOp.OR), Shapes.box(0.375, 0.3125, 0.375, 0.625, 0.375, 0.625), BooleanOp.OR));
        put(Direction.NORTH, Shapes.join(Shapes.join(Shapes.empty(), Shapes.box(0.375, 0.375, 0.375, 0.625, 0.625, 1), BooleanOp.OR), Shapes.box(0.375, 0.3125, 0.375, 0.625, 0.375, 0.625), BooleanOp.OR));
        put(Direction.EAST, Shapes.join(Shapes.join(Shapes.empty(), Shapes.box(0, 0.375, 0.375, 0.625, 0.625, 0.625), BooleanOp.OR), Shapes.box(0.375, 0.3125, 0.375, 0.625, 0.375, 0.625), BooleanOp.OR));
        put(Direction.SOUTH, Shapes.join(Shapes.join(Shapes.empty(), Shapes.box(0.375, 0.375, 0, 0.625, 0.625, 0.625), BooleanOp.OR), Shapes.box(0.375, 0.3125, 0.375, 0.625, 0.375, 0.625), BooleanOp.OR));
    }};

    public FoundryTapBlock(Properties properties) {
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
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
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
        return new FoundryTapBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, MetalworksRegistrator.FOUNDRY_TAP_BLOCK_ENTITY.get(), FoundryTapBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FoundryTapBlockEntity blockEntity) {
            if (!level.isClientSide) {
                blockEntity.toggleActive();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof FoundryTapBlockEntity blockEntity) {
            boolean hasSignal = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
            if (hasSignal) {
                blockEntity.toggleActive();
            }
        }
    }
}

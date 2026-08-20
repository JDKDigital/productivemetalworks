package cy.jdkdigital.productivemetalworks.common.block;

import com.mojang.serialization.MapCodec;
import cy.jdkdigital.productivemetalworks.common.block.entity.CastingBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

import javax.annotation.Nonnull;

public class CastingTableBlock extends BaseEntityBlock
{
    public static final MapCodec<CastingTableBlock> CODEC = simpleCodec(CastingTableBlock::new);
    private static final VoxelShape INSIDE1 = box(3.0D, 0.0D, 0.0D, 13.0D, 12.0D, 16.0D);
    private static final VoxelShape INSIDE2 = box(0.0D, 0.0D, 3.0D, 0.0D, 12.0D, 13.0D);
    protected static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(INSIDE1, INSIDE2), BooleanOp.ONLY_FIRST);

    public CastingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CastingBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, MetalworksRegistrator.CASTING_BLOCK_ENTITY.get(), level.isClientSide() ? CastingBlockEntity::clientTick : CastingBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel && serverLevel.getBlockEntity(pos) instanceof CastingBlockEntity blockEntity && !blockEntity.isCooling() && blockEntity.getFluidHandler().getFluidAmount() == 0) {
            // Take output first, if there's no output grab the cast
            var outputItem = blockEntity.getResultStack();
            if (outputItem.isEmpty()) {
                outputItem = blockEntity.castInv.getStackInSlot(0);
            }

            if (!outputItem.isEmpty()) {
                if (!player.getInventory().add(outputItem.copy())) {
                    Block.popResourceFromFace(serverLevel, pos, Direction.UP, outputItem.copy());
                }
                blockEntity.clearResultOrCast();
                blockEntity.sync(serverLevel);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (
                level.getBlockEntity(pos) instanceof CastingBlockEntity blockEntity &&
                blockEntity.canAcceptCast() // no fluid
        ) {
            if (level instanceof ServerLevel serverLevel) {
                var clonedStack = stack.copy();
                clonedStack.setCount(1);
                // Only consume the held item if it was actually stored — never void it on a failed insert.
                if (blockEntity.castInv.insertItem(0, clonedStack, false).isEmpty()) {
                    stack.shrink(1);
                    blockEntity.sync(serverLevel);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}

package cy.jdkdigital.productivemetalworks.common.block;

import com.mojang.serialization.MapCodec;
import cy.jdkdigital.productivemetalworks.common.block.entity.CastingBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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
        return createTickerHelper(blockEntityType, MetalworksRegistrator.CASTING_BLOCK_ENTITY.get(), level.isClientSide ? CastingBlockEntity::clientTick : CastingBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel && serverLevel.getBlockEntity(pos) instanceof CastingBlockEntity blockEntity) {
            var itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            // Take output first, if there's no output grab the cast
            var outputItem = blockEntity.getItemHandler().getStackInSlot(0);
            if (outputItem.isEmpty()) {
                outputItem = blockEntity.castInv.getStackInSlot(0);
            }
            if (
                    (
                            itemInHand.isEmpty() ||
                            ItemStack.isSameItemSameComponents(itemInHand, outputItem)
                    ) &&
                    blockEntity.getFluidHandler().getFluidAmount() == 0 &&
                    !blockEntity.isCooling()
            ) {
                if (!outputItem.isEmpty()) {
                    if (itemInHand.isEmpty()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, outputItem.copy());
                    } else {
                        itemInHand.grow(outputItem.getCount());
                    }
                    outputItem.shrink(outputItem.getMaxStackSize());
                    blockEntity.sync(serverLevel);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (
                level.getBlockEntity(pos) instanceof CastingBlockEntity blockEntity &&
                blockEntity.getItemHandler().getStackInSlot(0).isEmpty() && // no crafted output
                blockEntity.castInv.getStackInSlot(0).isEmpty() && // no cast
                blockEntity.getFluidHandler().getFluidAmount() == 0 // no fluid
        ) {
            boolean isTable = state.is(MetalworksRegistrator.CASTING_TABLE);
            if (level instanceof ServerLevel serverLevel && (!isTable || !(stack.getItem() instanceof BlockItem))) {
                var clonedStack = stack.copy();
                clonedStack.setCount(1);
                blockEntity.castInv.insertItem(0, clonedStack, false);
                stack.shrink(1);
                blockEntity.sync(serverLevel);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof CastingBlockEntity blockEntity) {
                // Drop inventory
                if (!blockEntity.isCooling()) {
                    for (int slot = 0; slot < blockEntity.getItemHandler().getSlots(); ++slot) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), blockEntity.getItemHandler().getStackInSlot(slot));
                    }
                }
                for (int slot = 0; slot < blockEntity.castInv.getSlots(); ++slot) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), blockEntity.castInv.getStackInSlot(slot));
                }
            }
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }
}

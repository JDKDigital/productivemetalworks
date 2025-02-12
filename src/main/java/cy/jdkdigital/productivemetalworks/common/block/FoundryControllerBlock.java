package cy.jdkdigital.productivemetalworks.common.block;

import com.mojang.serialization.MapCodec;
import cy.jdkdigital.productivelib.common.block.CapabilityContainerBlock;
import cy.jdkdigital.productivelib.common.block.IMultiBlockController;
import cy.jdkdigital.productivelib.exception.InvalidStructureException;
import cy.jdkdigital.productivelib.util.MultiBlockDetector;
import cy.jdkdigital.productivemetalworks.Config;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryControllerBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class FoundryControllerBlock extends CapabilityContainerBlock implements IMultiBlockController
{
    public static final MapCodec<FoundryControllerBlock> CODEC = simpleCodec(FoundryControllerBlock::new);

    public FoundryControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.ATTACHED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(BlockStateProperties.ATTACHED).add(BlockStateProperties.HORIZONTAL_FACING);
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FoundryControllerBlockEntity(blockPos, blockState);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(BlockStateProperties.ATTACHED)) {
            double d0 = (double)pos.getX() + 0.5;
            double d1 = (double)pos.getY();
            double d2 = (double)pos.getZ() + 0.5;
            Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction.Axis direction$axis = direction.getAxis();
            double d4 = random.nextDouble() * 0.6 - 0.3;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52 : d4;
            double d6 = random.nextDouble() * 6.0 / 16.0;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52 : d4;
            level.addParticle(ParticleTypes.FLAME, d0 + d5, d1 + d6, d2 + d7, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof ServerLevel serverLevel && serverLevel.getBlockEntity(pos) instanceof FoundryControllerBlockEntity blockEntity) {
            try {
                var foundryData = MultiBlockDetector.detectStructure(serverLevel, pos, ModTags.Blocks.FOUNDRY_WALL_BLOCKS, ModTags.Blocks.FOUNDRY_BOTTOM_BLOCKS, true, true, Config.foundryMaxVolume, Config.foundryMaxCircumference, Config.foundryMaxHeight);
                blockEntity.setMultiBlockData(foundryData);
            } catch (InvalidStructureException ise) {
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, MetalworksRegistrator.FOUNDRY_CONTROLLER_BLOCK_ENTITY.get(), level.isClientSide ? FoundryControllerBlockEntity::clientTick : FoundryControllerBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FoundryControllerBlockEntity blockEntity) {
            try {
                var foundryData = MultiBlockDetector.detectStructure(level, pos, ModTags.Blocks.FOUNDRY_WALL_BLOCKS, ModTags.Blocks.FOUNDRY_BOTTOM_BLOCKS, true, true, Config.foundryMaxVolume, Config.foundryMaxCircumference, Config.foundryMaxHeight);
                blockEntity.setMultiBlockData(foundryData);
//                    player.sendSystemMessage(Component.translatable(ProductiveMetalworks.MODID + ".message.foundry_formed"));
                if (!level.isClientSide) {
                    player.openMenu(blockEntity, pos);
                }
            } catch (InvalidStructureException ise) {
                player.sendSystemMessage(Component.translatable(ProductiveMetalworks.MODID + ".message.foundry_invalid", ise.getMessage(), "" + level.getBlockState(ise.getPos())));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof FoundryControllerBlockEntity blockEntity) {
                // Drop inventory
                for (int slot = 0; slot < blockEntity.getItemHandler().getSlots(); ++slot) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), blockEntity.getItemHandler().getStackInSlot(slot));
                }
            }
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }
}

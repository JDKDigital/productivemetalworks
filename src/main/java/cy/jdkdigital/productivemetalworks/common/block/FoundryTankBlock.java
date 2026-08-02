package cy.jdkdigital.productivemetalworks.common.block;

import com.mojang.serialization.MapCodec;
import cy.jdkdigital.productivelib.common.block.IMultiBlockPeripheral;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTankBlockEntity;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.Nullable;

public class FoundryTankBlock extends BaseEntityBlock implements IMultiBlockPeripheral
{
    public static final MapCodec<FoundryTankBlock> CODEC = simpleCodec(FoundryTankBlock::new);

    public FoundryTankBlock(Properties properties) {
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
        return new FoundryTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, MetalworksRegistrator.FOUNDRY_TANK_BLOCK_ENTITY.get(), FoundryTankBlockEntity::serverTick);
    }

    // PORT-TODO (26.1): tooltip moved off Block (appendHoverText removed). Reinstate via a custom
    //  BlockItem subclass that reads the FLUID_STACK data component.

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Run on both sides (interactWithFluidHandler commits, so the client prediction matches the server)
        // and return SUCCESS to suppress the bucket's default place-fluid action. Without the client-side
        // path the client falls through and mis-places the fluid as a block. Gravity transfer
        // (tickFluidTank) then spreads the fluid across the stacked tank column.
        if (level.getBlockEntity(pos) instanceof FoundryTankBlockEntity blockEntity
                && FluidUtil.interactWithFluidHandler(player, hand, pos, blockEntity.getFluidHandler())) {
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}

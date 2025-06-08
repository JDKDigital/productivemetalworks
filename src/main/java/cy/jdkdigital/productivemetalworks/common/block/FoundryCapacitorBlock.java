package cy.jdkdigital.productivemetalworks.common.block;

import com.mojang.serialization.MapCodec;
import cy.jdkdigital.productivelib.common.block.IMultiBlockPeripheral;
import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryCapacitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
//        if (stack.has(MetalworksRegistrator.FLUID_STACK.get())) {
//            var fluid = stack.get(MetalworksRegistrator.FLUID_STACK.get());
//            if (fluid != null) {
//                tooltipComponents.add(Component.translatable("block." + ProductiveMetalworks.MODID + "foundry_tank.fluid_tooltip", fluid.fluid().getAmount(), Component.translatable(fluid.fluid().getFluidType().getDescriptionId())).withStyle(ChatFormatting.LIGHT_PURPLE));
//            }
//        }
    }
}

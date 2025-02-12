package cy.jdkdigital.productivemetalworks.common.block;

import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MeatBlock extends RotatedPillarBlock
{
    public static final IntegerProperty BITES = IntegerProperty.create("meat_bites", 0, 7);

    protected static final VoxelShape[] SHAPE_BY_BITE = new VoxelShape[]{
            Block.box(1.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(3.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(5.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(7.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(9.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(11.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(13.0, 0.0, 1.0, 15.0, 8.0, 15.0)
    };

    public MeatBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0).setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BITES);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return eat(level, pos, state, player);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            level.playSound(player, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS);
        }
        super.attack(state, level, pos, player);
    }

    protected static InteractionResult eat(Level level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false) || player.getUseItem().is(MetalworksRegistrator.MEAT_BLOCK.get().asItem())) {
            return InteractionResult.PASS;
        } else {
            player.getFoodData().eat(2, 0.1F);
            level.gameEvent(player, GameEvent.EAT, pos);

            if (level instanceof ServerLevel) {
                int i = state.getValue(BITES);
                if (i < 7) {
                    level.setBlock(pos, state.setValue(BITES, i + 1), Block.UPDATE_ALL);
                } else {
                    level.removeBlock(pos, false);
                    level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
                    Block.popResource(level, pos, Items.BONE.getDefaultInstance());
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }
}

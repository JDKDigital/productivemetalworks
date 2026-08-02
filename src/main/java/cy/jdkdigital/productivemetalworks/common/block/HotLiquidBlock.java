package cy.jdkdigital.productivemetalworks.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class HotLiquidBlock extends LiquidBlock
 {
     public HotLiquidBlock(FlowingFluid fluid, Properties properties) {
         super(fluid, properties);
     }

     @Override
     protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
         super.entityInside(state, level, pos, entity, effectApplier, isPrecise);

         entity.setRemainingFireTicks(100);
     }
 }

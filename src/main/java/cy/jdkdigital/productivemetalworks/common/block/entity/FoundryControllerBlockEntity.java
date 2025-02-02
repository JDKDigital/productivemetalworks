package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.FluidTankBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.IMultiBlockControllerBlockEntity;
import cy.jdkdigital.productivelib.exception.InvalidStructureException;
import cy.jdkdigital.productivelib.util.MultiBlockDetector;
import cy.jdkdigital.productivelib.util.MultiFluidTank;
import cy.jdkdigital.productivemetalworks.Config;
import cy.jdkdigital.productivemetalworks.common.menu.FoundryControllerContainer;
import cy.jdkdigital.productivemetalworks.recipe.FluidAlloyingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.ItemMeltingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import cy.jdkdigital.productivemetalworks.util.RecipeHelper;
import cy.jdkdigital.productivemetalworks.util.TickingSlotInventoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class FoundryControllerBlockEntity extends FluidTankBlockEntity implements IMultiBlockControllerBlockEntity, MenuProvider
{
    private MultiBlockDetector.MultiBlockData foundryData;
    private int tickCounter = 0;
    private float leftoverTick = 0;

    // used clientside for rendering fuel in gui screen
    public FluidStack fuel = FluidStack.EMPTY;

    public MultiFluidTank fluidHandler = new MultiFluidTank(20, 90000) {
        @Override
        protected void onContentsChanged(boolean hasChangedFluid) {
            super.onContentsChanged(hasChangedFluid);
            if (hasChangedFluid && FoundryControllerBlockEntity.this.level instanceof ServerLevel serverLevel) {
                FoundryControllerBlockEntity.this.sync(serverLevel);
            }
            FoundryControllerBlockEntity.this.setChanged();
        }
    };

    protected TickingSlotInventoryHandler itemHandler = new TickingSlotInventoryHandler(200, this) {
        @Override
        protected int getTimeInSlot(ItemStack stack) {
            if (this.blockEntity != null && blockEntity.getLevel() instanceof Level pLevel) {
                RecipeHolder<ItemMeltingRecipe> recipe = RecipeHelper.getItemMeltingRecipe(pLevel, stack);
                if (recipe != null) {
                    return recipe.value().result.stream().map(FluidStack::getAmount).reduce(Integer::sum).orElse(0);
                }
            }
            return 0;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isInputSlotItem(int slot, ItemStack item) {
            return true;
        }

        @Override
        public boolean isInputSlot(int slot) {
            return true;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }

        @Override
        public int[] getOutputSlots() {
            return new int[]{};
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (FoundryControllerBlockEntity.this.level instanceof ServerLevel serverLevel) {
                FoundryControllerBlockEntity.this.sync(serverLevel);
            }
        }
    };

    public FoundryControllerBlockEntity(BlockPos pos, BlockState state) {
        super(MetalworksRegistrator.FOUNDRY_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryControllerBlockEntity blockEntity) {
        if (blockEntity.getMultiblockData() != null) {
            var mb = blockEntity.getMultiblockData();
            FluidTankBlockEntity.tick(level, pos, state, blockEntity);
            if (blockEntity.tickCounter%20 == 0 && (Config.foundryCollectItems || Config.foundryDamageEntities)) {
                var c1 = mb.topCorners().getFirst().below(mb.height() - 1);
                var c2 = mb.topCorners().getSecond().below(mb.height() - 2);

                level.getEntities(null, new AABB(c1.getX(), c1.getY(), c1.getZ(), c2.getX(), c2.getY(), c2.getZ())).forEach(entity -> {
                    // Item entities can be picked up when there's room in the item handler
                    if (Config.foundryCollectItems) {
                        if (entity instanceof ItemEntity item) {
                            var groundStack = item.getItem();
                            for (int slot = 0; slot < blockEntity.itemHandler.getSlots(); slot++) {
                                if (blockEntity.itemHandler.getItem(slot).isEmpty()) {
                                    blockEntity.itemHandler.setStackInSlot(slot, new ItemStack(groundStack.getItem(), 1));
                                    groundStack.shrink(1);
                                }
                            }
                        }
                    }
                    if (Config.foundryDamageEntities) {
                        // Other entities take damage if there's heat
                        if (entity instanceof LivingEntity livingEntity && blockEntity.getFluidHandler().totalFluidAmount() > 0) {
                            livingEntity.hurt(level.damageSources().hotFloor(), 2.0f);
                        }
                    }
                });
            }

            // Melt items from inventory if they have a melting recipe
            var fuel = blockEntity.getFuel();
            if (!fuel.isEmpty()) {
                var fuelData = fuel.getFluid().builtInRegistryHolder().getData(MetalworksRegistrator.FUEL_MAP);
                if (fuelData != null) {
                    int burnTicks = Math.round(fuelData.speed() + blockEntity.leftoverTick);
                    blockEntity.leftoverTick = fuelData.speed() + blockEntity.leftoverTick - burnTicks;
                    blockEntity.itemHandler.tick(burnTicks);
                    boolean hasChanged = false;
                    FluidStack consumedFuel = new FluidStack(fuel.getFluid(), 0);
                    for (int slot = 0; slot < blockEntity.itemHandler.size(); slot++) {
                        var ticker = blockEntity.itemHandler.getTicker(slot);
                        if (ticker.getSecond() > 0 && ticker.getFirst() == 0) {
                            var item = blockEntity.getItemHandler().getStackInSlot(slot);
                            if (!item.isEmpty()) {
                                RecipeHolder<ItemMeltingRecipe> recipe = RecipeHelper.getItemMeltingRecipe(level, item);
                                if (recipe != null) {
                                    int totalProducedFluid = recipe.value().result.stream().map(FluidStack::getAmount).reduce(Integer::sum).orElse(0);
                                    if (totalProducedFluid + consumedFuel.getAmount() <= fuel.getAmount() && totalProducedFluid <= blockEntity.fluidHandler.getCapacity() - blockEntity.fluidHandler.totalFluidAmount()) {
                                        consumedFuel.grow((int)(totalProducedFluid * fuelData.consumption()));
                                        blockEntity.itemHandler.extractItem(slot, 1, false, false);
                                        hasChanged = true;
                                        for (FluidStack fluidStack : recipe.value().result) {
                                            if (blockEntity.fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) == fluidStack.getAmount()) {
                                                blockEntity.fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (hasChanged) {
                        if (!consumedFuel.isEmpty()) {
                            for (BlockPos blockPos : mb.peripherals()) {
                                if (!consumedFuel.isEmpty() && level.getBlockEntity(blockPos) instanceof FoundryTankBlockEntity tankBlockEntity) {
                                    var drainedFluid = tankBlockEntity.getFluidHandler().drain(consumedFuel, IFluidHandler.FluidAction.EXECUTE);
                                    consumedFuel.shrink(drainedFluid.getAmount());
                                }
                            }
                        }
                        blockEntity.sync(level);
                    }
                }
            }
        }

        if (++blockEntity.tickCounter%200 == 0) {// TODO scaling number based on failures
            blockEntity.tickCounter = 0;
            try {
                var foundryData = MultiBlockDetector.detectStructure(level, pos, ModTags.Blocks.FOUNDRY_WALL_BLOCKS, ModTags.Blocks.FOUNDRY_BOTTOM_BLOCKS, true, true, Config.foundryMaxVolume, Config.foundryMaxCircumference, Config.foundryMaxHeight);
                blockEntity.setMultiBlockData(foundryData);
            } catch (InvalidStructureException e) {
                blockEntity.setMultiBlockData(null);
            }
        }
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, FoundryControllerBlockEntity blockEntity) {
        var fuel = blockEntity.getFuel();
        if (!fuel.isEmpty()) {
            var fuelData = fuel.getFluid().builtInRegistryHolder().getData(MetalworksRegistrator.FUEL_MAP);
            if (fuelData != null) {
                int burnTicks = Math.round(fuelData.speed() + blockEntity.leftoverTick);
                blockEntity.leftoverTick = fuelData.speed() + blockEntity.leftoverTick - burnTicks;
                blockEntity.itemHandler.tick(burnTicks);
            }
        }
    }

    @Override
    public void tickFluidTank(Level level, BlockPos blockPos, BlockState blockState, FluidTankBlockEntity fluidTankBlockEntity) {
        // TODO refresh recipeProcessList every few seconds instead of each tick
        List<RecipeHolder<FluidAlloyingRecipe>> recipeProcessList = RecipeHelper.getAlloyRecipes(level, fluidHandler);

        recipeProcessList.forEach(fluidAlloyingRecipe -> {
            int speed = fluidAlloyingRecipe.value().speed;
            boolean canDrainFullSpeed = fluidAlloyingRecipe.value().fluids.stream().map(f -> new SizedFluidIngredient(f.ingredient(), f.amount() * speed)).noneMatch(fluid -> fluidHandler.drain(fluid, IFluidHandler.FluidAction.SIMULATE).isEmpty());
            if (canDrainFullSpeed && fluidHandler.fill(new FluidStack(fluidAlloyingRecipe.value().result.getFluid(), fluidAlloyingRecipe.value().result.getAmount() * speed), IFluidHandler.FluidAction.SIMULATE) > 0) {
                fluidAlloyingRecipe.value().fluids.forEach(fluid -> fluidHandler.drain(fluid, IFluidHandler.FluidAction.EXECUTE));
                fluidHandler.fill(fluidAlloyingRecipe.value().result, IFluidHandler.FluidAction.EXECUTE);
            } else {
                boolean canDrain = fluidAlloyingRecipe.value().fluids.stream().noneMatch(fluid -> fluidHandler.drain(fluid, IFluidHandler.FluidAction.SIMULATE).isEmpty());
                if (canDrain && fluidHandler.fill(fluidAlloyingRecipe.value().result, IFluidHandler.FluidAction.SIMULATE) > 0) {
                    fluidAlloyingRecipe.value().fluids.forEach(fluid -> fluidHandler.drain(fluid, IFluidHandler.FluidAction.EXECUTE));
                    fluidHandler.fill(fluidAlloyingRecipe.value().result, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        });
    }

    @Override
    public int tankTickRate() {
        return 1;
    }

    @Override
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public MultiFluidTank getFluidHandler() {
        return fluidHandler;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new FoundryControllerContainer(i, inventory, this);
    }

    @Override
    public void savePacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.savePacketNBT(tag, provider);

        if (this.getMultiblockData() != null) {
            tag.put("multiData", this.getMultiblockData().serializeNBT(provider));
        }
    }

    @Override
    public void loadPacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadPacketNBT(tag, provider);

        if (tag.contains("multiData")) {
            var data = new MultiBlockDetector.MultiBlockData(null, null, List.of(), 0, 0);
            data.deserializeNBT(provider, Objects.requireNonNull(tag.get("multiData")));
            setMultiBlockData(data);
        }
    }

    public FluidStack getFuel() {
        var mb = getMultiblockData();
        if (level != null && mb != null) {
            FluidStack fuel = FluidStack.EMPTY;
            for (BlockPos pos: mb.peripherals()) {
                if (level.getBlockEntity(pos) instanceof FoundryTankBlockEntity tank) {
                    var fluid = tank.getFluidHandler().getFluidInTank(0);
                    if (!fluid.isEmpty()) {
                        if (fuel.isEmpty()) {
                            fuel = fluid.copy();
                        } else if (fuel.is(fluid.getFluid())) {
                            fuel.grow(fluid.getAmount());
                        }
                    }
                }
            }
            return fuel;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void setMultiBlockData(MultiBlockDetector.MultiBlockData multiBlockData) {
        // Set tank size based on structure volume
        if (level instanceof ServerLevel serverLevel) {
            if (multiBlockData != null) {
                this.fluidHandler.setCapacity(multiBlockData.volume() * Config.foundryFluidCapacityPerBlockVolume);
                // if the inventory has shrunk and was full, then items are lost...TODO maybe handle that
                this.itemHandler.setSize(multiBlockData.volume());

                if (!getBlockState().getValue(BlockStateProperties.ATTACHED)) {
                    serverLevel.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlockStateProperties.ATTACHED, true));
                }

                // turn on heating coils
                var c1 = multiBlockData.topCorners().getFirst().below(multiBlockData.height());
                var c2 = multiBlockData.topCorners().getSecond().below(multiBlockData.height());

                BlockPos.betweenClosed(c1, c2).forEach(blockPos -> {
                    var state = serverLevel.getBlockState(blockPos);
                    if (state.is(ModTags.Blocks.HEATING_COILS) && !state.getValue(BlockStateProperties.ATTACHED)) {
                        serverLevel.setBlockAndUpdate(blockPos, state.setValue(BlockStateProperties.ATTACHED, true));
                    }
                });
            } else if (getBlockState().getValue(BlockStateProperties.ATTACHED)) {
                serverLevel.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlockStateProperties.ATTACHED, false));
            }
        } else {
            // update client side
            this.fluidHandler.setCapacity(multiBlockData.volume() * Config.foundryFluidCapacityPerBlockVolume);
            this.itemHandler.setSize(multiBlockData.volume());
        }

        // sync if the multiblock is formed or has changed from/to formed
        if (level instanceof ServerLevel serverLevel && (this.foundryData != multiBlockData || multiBlockData != null)) {
            this.sync(serverLevel);
        }
        this.foundryData = multiBlockData;
        this.setChanged();
    }

    @Override
    public MultiBlockDetector.MultiBlockData getMultiblockData() {
        return this.foundryData;
    }

    public void sync(Level level) {
        // TODO move to lib and schedule this so it's not called multiple times in the same tick
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void moveTankFirst(int tank) {
        if (tank > 0 && tank < this.fluidHandler.getTanks()) {
            this.fluidHandler.moveTankToTop(tank);
            this.sync(level);
        }
    }
}

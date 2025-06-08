package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.FluidTankBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.IMultiBlockControllerBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.IUpgradeableBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.InventoryHandlerHelper;
import cy.jdkdigital.productivelib.exception.InvalidStructureException;
import cy.jdkdigital.productivelib.registry.LibItems;
import cy.jdkdigital.productivelib.util.MultiBlockDetector;
import cy.jdkdigital.productivelib.util.MultiFluidTank;
import cy.jdkdigital.productivemetalworks.Config;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.common.block.FoundryControllerBlock;
import cy.jdkdigital.productivemetalworks.common.block.IHeatingCoilBlock;
import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;
import cy.jdkdigital.productivemetalworks.common.menu.FoundryControllerContainer;
import cy.jdkdigital.productivemetalworks.recipe.FluidAlloyingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.ItemMeltingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import cy.jdkdigital.productivemetalworks.util.CoilType;
import cy.jdkdigital.productivemetalworks.util.IFoundryFuel;
import cy.jdkdigital.productivemetalworks.util.RecipeHelper;
import cy.jdkdigital.productivemetalworks.util.TickingSlotInventoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.TickPriority;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class FoundryControllerBlockEntity extends FluidTankBlockEntity implements IUpgradeableBlockEntity, IMultiBlockControllerBlockEntity, MenuProvider
{
    private MultiBlockDetector.MultiBlockData foundryData;
    private int tickCounter = 0;
    private float leftoverTick = 0;
    private CoilType coilType = CoilType.UNKNOWN;

    // used clientside for rendering fuel in gui screen
    public FluidStack fuel = FluidStack.EMPTY;

    public MultiFluidTank fluidHandler = new MultiFluidTank(20, 90000)
    {
        @Override
        protected void onContentsChanged(boolean hasChangedFluid) {
            super.onContentsChanged(hasChangedFluid);
            if (hasChangedFluid && FoundryControllerBlockEntity.this.level instanceof ServerLevel serverLevel) {
                FoundryControllerBlockEntity.this.sync(serverLevel);
            }
            FoundryControllerBlockEntity.this.setChanged();
        }
    };

    public CoilType getCoilType() {
        return coilType;
    }

    protected TickingSlotInventoryHandler itemHandler = new TickingSlotInventoryHandler(200, this)
    {
        @Override
        protected int getTimeInSlot(ItemStack stack) {
            FoundryControllerBlockEntity fbe = (FoundryControllerBlockEntity)this.blockEntity;

            IMelterProcessor mp;
            switch (fbe.coilType) {
                case CoilType.FLUID -> {
                    mp = new LiquidMelter();
                }
                case CoilType.ENERGY -> {
                    mp = new EnergyMelter();
                }
                default -> {return 0;}
            }

            if (this.blockEntity != null && blockEntity.getLevel() instanceof Level pLevel) {
                var fuelData = mp.getFoundryFuel(pLevel, fbe);//FoundryControllerBlockEntity.this.getFuel().getFluidHolder().getData(MetalworksRegistrator.FUEL_MAP);
                RecipeHolder<ItemMeltingRecipe> recipe = RecipeHelper.getItemMeltingRecipe(pLevel, stack, fuelData.getFuelData());
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

    protected IItemHandlerModifiable upgradeHandler = new InventoryHandlerHelper.UpgradeHandler(4, this, List.of(
            LibItems.UPGRADE_TIME.get(),
            LibItems.UPGRADE_TIME_2.get(),
            LibItems.UPGRADE_STABILITY.get()
    ));

    public FoundryControllerBlockEntity(BlockPos pos, BlockState state) {
        super(MetalworksRegistrator.FOUNDRY_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public IItemHandlerModifiable getUpgradeHandler() {
        return upgradeHandler;
    }

    protected void burnItemsAtSpeed(float burnSpeed) {
        int burnTicks = Math.round(burnSpeed + this.leftoverTick);
        this.leftoverTick = burnSpeed + this.leftoverTick - burnTicks;
        this.itemHandler.tick(burnTicks);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryControllerBlockEntity blockEntity) {
        if (blockEntity.getMultiblockData() != null) {
            FluidTankBlockEntity.tick(level, pos, state, blockEntity);

            var mb = blockEntity.getMultiblockData();

            // Process entity detection
            if (blockEntity.tickCounter % 20 == 0 && (Config.foundryCollectItems || Config.foundryDamageEntities)) {
                var c1 = mb.topCorners().getFirst().below(mb.height() - 1);
                var c2 = mb.topCorners().getSecond().below(mb.height() - 2);

                level.getEntities(null, new AABB(c1.getX(), c1.getY(), c1.getZ(), c2.getX(), c2.getY(), c2.getZ())).forEach(entity -> {
                    // Item entities can be picked up when there's room in the item handler
                    if (Config.foundryCollectItems) {
                        if (entity instanceof ItemEntity item) {
                            var groundStack = item.getItem();
                            for (int slot = 0; slot < blockEntity.itemHandler.getSlots(); slot++) {
                                if (blockEntity.itemHandler.getItem(slot).isEmpty()) {
                                    var clonedStack = groundStack.copy();
                                    clonedStack.setCount(1);
                                    blockEntity.itemHandler.setStackInSlot(slot, clonedStack);
                                    groundStack.shrink(1);
                                }
                            }
                        }
                    }
                    if (Config.foundryDamageEntities) {
                        // Other entities take damage if there's heat
                        if (entity instanceof LivingEntity livingEntity && (blockEntity.getFluidHandler().totalFluidAmount() > 0 || blockEntity.getFuel().getAmount() > 0)) {
                            livingEntity.hurt(level.damageSources().hotFloor(), 2.0f);
                            var meltingFluid = livingEntity.getType().builtInRegistryHolder().getData(MetalworksRegistrator.ENTITY_MELTING_MAP);
                            if (meltingFluid != null) {
                                blockEntity.fluidHandler.fill(meltingFluid.fluid(), IFluidHandler.FluidAction.EXECUTE);
                            }
                        }
                    }
                });
            }

            IMelterProcessor mp;
            switch (blockEntity.coilType) {
                case CoilType.FLUID -> {
                    mp = new LiquidMelter();
                    mp.tick(level, pos, state, blockEntity);
                }
                case CoilType.ENERGY -> {
                    mp = new EnergyMelter();
                    mp.tick(level, pos, state, blockEntity);
                }
            }
        }

        if (++blockEntity.tickCounter % 200 == 0) {// TODO scaling number based on failures
            blockEntity.tickCounter = 0;
            try {
                blockEntity.setMultiBlockData(FoundryControllerBlock.detectMultiblock(level, pos));
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
                float burnSpeed = fuelData.speed() * blockEntity.getSpeedModifier();
                int burnTicks = Math.round(burnSpeed + blockEntity.leftoverTick);
                blockEntity.leftoverTick = burnSpeed + blockEntity.leftoverTick - burnTicks;
                blockEntity.itemHandler.tick(burnTicks);
            }
        }
    }

    private int getSpeedModifier() {
        return 1 + this.getUpgradeCount(LibItems.UPGRADE_TIME.get()) + 2 * this.getUpgradeCount(LibItems.UPGRADE_TIME_2.get());
    }

    @Override
    public void tickFluidTank(Level level, BlockPos blockPos, BlockState blockState, FluidTankBlockEntity fluidTankBlockEntity) {
        // TODO refresh recipeProcessList every few seconds instead of each tick
        if (fluidTankBlockEntity instanceof FoundryControllerBlockEntity foundry && foundry.getUpgradeCount(LibItems.UPGRADE_STABILITY.get()) == 0) {
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
        if (level == null || mb == null) {
            return FluidStack.EMPTY;
        }

        FluidStack fluidFuel = FluidStack.EMPTY;
        for (BlockPos pos : mb.peripherals()) {
            if (level.getBlockEntity(pos) instanceof FoundryTankBlockEntity tank) {
                var fluid = tank.getFluidHandler().getFluidInTank(0);
                if (!fluid.isEmpty()) {
                    if (fluidFuel.isEmpty()) {
                        fluidFuel  = fluid.copy();
                    } else if (fluidFuel.is(fluid.getFluid())) {
                        fluidFuel.grow(fluid.getAmount());
                    }
                }
            }
        }
        return fluidFuel;
    }

    public int getPower() {
        var mb = getMultiblockData();
        if (level == null || mb == null) {
            return 0;
        }

        int power = 0;
        for (BlockPos pos : mb.peripherals()) {
            if (level.getBlockEntity(pos) instanceof FoundryCapacitorBlockEntity capacitor) {
                power += capacitor.energyHandler.getEnergyStored();
            }
        }
        return power;
    }

    public int getPowerMax() {
        var mb = getMultiblockData();
        if (level == null || mb == null) {
            return 0;
        }

        int power = 0;
        for (BlockPos pos : mb.peripherals()) {
            if (level.getBlockEntity(pos) instanceof FoundryCapacitorBlockEntity capacitor) {
                power += capacitor.energyHandler.getMaxEnergyStored();
            }
        }
        return power;
    }

    void addRecipeResult(List<FluidStack> result) {
        for (FluidStack fluidStack : result) {
            if (this.fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) == fluidStack.getAmount()) {
                this.fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            }
        }
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

                this.coilType = CoilType.UNKNOWN;
                BlockPos.betweenClosed(c1, c2).forEach(blockPos -> {
                    var state = serverLevel.getBlockState(blockPos);
                    if (state.is(ModTags.Blocks.HEATING_COILS) && !state.getValue(BlockStateProperties.ATTACHED)) {
                        serverLevel.setBlockAndUpdate(blockPos, state.setValue(BlockStateProperties.ATTACHED, true));
                    }
                    if (state.getBlock() instanceof IHeatingCoilBlock heatingCoilBlock) {
                        this.coilType = heatingCoilBlock.getCoilType();
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

        // Update drains to trigger comparator updates
        var mb = getMultiblockData();
        if (mb != null) {
            mb.peripherals().forEach(pos -> {
                var peripheralBlockState = level.getBlockState(pos);
                if (peripheralBlockState.is(ModTags.Blocks.FOUNDRY_DRAINS)) {
                    for (Direction direction : Direction.values()) {
                        var relBlockState = level.getBlockState(pos.relative(direction));
                        if (relBlockState.is(Blocks.COMPARATOR) && !level.getBlockTicks().willTickThisTick(pos.relative(direction), relBlockState.getBlock())) {
                            level.scheduleTick(pos.relative(direction), relBlockState.getBlock(), 2, TickPriority.NORMAL);
                        }
                    }
                }
            });
        }
    }

    public void moveTankFirst(int tank) {
        if (tank > 0 && tank < this.fluidHandler.getTanks()) {
            this.fluidHandler.moveTankToTop(tank);
            this.sync(level);
        }
    }

    interface IMelterProcessor
    {
        void tick(Level level, BlockPos pos, BlockState state, FoundryControllerBlockEntity blockEntity);

        IFoundryFuel getFoundryFuel(Level level, FoundryControllerBlockEntity blockEntity);

        default boolean meltItems(Level level, FoundryControllerBlockEntity blockEntity, IFoundryFuel consumedFuel, IFoundryFuel availableFuel) {
            int speedModifier = blockEntity.getSpeedModifier();
            float burnSpeed = availableFuel.getFuelData().speed() * speedModifier;

            // Update slots
            blockEntity.burnItemsAtSpeed(burnSpeed);

            boolean hasChanged = false;
            for (int slot = 0; slot < blockEntity.itemHandler.size(); slot++) {
                // Get timer
                var ticker = blockEntity.itemHandler.getTicker(slot);
                if (ticker.getSecond() <= 0 || ticker.getFirst() > 0) {
                    continue;
                }

                // Get item in Slot
                var item = blockEntity.getItemHandler().getStackInSlot(slot);
                if (item.isEmpty()) {
                    continue;
                }

                // Get Recipe for item in slot
                RecipeHolder<ItemMeltingRecipe> recipe = RecipeHelper.getItemMeltingRecipe(level, item, availableFuel.getFuelData());
                if (recipe == null) {
                    continue;
                }

                // Sum total fluid amount that would be melted
                int totalProducedFluid = recipe.value().result.stream().map(FluidStack::getAmount).reduce(Integer::sum).orElse(0);

                // Look at the fuel required for this recipe melt
                int requiredFuel = (int) (totalProducedFluid * availableFuel.getFuelData().consumption() * speedModifier);

                // Do not proceed if fuel required would exceed total capacity
                if (requiredFuel + consumedFuel.getAmount() > availableFuel.getAmount()) {
                    continue;
                }

                // Do not proceed if liquid melted would exceed the total forge capacity
                if (totalProducedFluid > blockEntity.fluidHandler.getCapacity() - blockEntity.fluidHandler.totalFluidAmount()) {
                    continue;
                }

                // Actually consume the fuel and remove the item
                consumedFuel.grow(requiredFuel);
                blockEntity.itemHandler.extractItem(slot, 1, false, false);
                hasChanged = true;

                // Melt every result into the tank
                blockEntity.addRecipeResult(recipe.value().result);
            }
            return hasChanged;
        }
    }

    static class LiquidMelter implements IMelterProcessor
    {
        public void tick(Level level, BlockPos pos, BlockState state, FoundryControllerBlockEntity blockEntity) {
            var mb = blockEntity.getMultiblockData();

            // Melt items from inventory if they have a melting recipe
            FluidStack fuelStack = blockEntity.getFuel();
            if (fuelStack.isEmpty()) {
                return;
            }

            IFoundryFuel fuel = new FluidFuel(fuelStack);
            if (fuel.getFuelData() == null) {
                return;
            }

            // Create an accumulator to count how much fuel will be used
            IFoundryFuel consumedFuel = new FluidFuel(new FluidStack(fuelStack.getFluid(), 0));

            boolean hasChanged = meltItems(level, blockEntity, consumedFuel, fuel);

            if (hasChanged) {
                for (BlockPos blockPos : mb.peripherals()) {
                    if (!consumedFuel.isEmpty() && level.getBlockEntity(blockPos) instanceof FoundryTankBlockEntity tankBlockEntity) {
                        var drainedFluid = tankBlockEntity.getFluidHandler().drain(new FluidStack(fuelStack.getFluid(), consumedFuel.getAmount()), IFluidHandler.FluidAction.EXECUTE);
                        consumedFuel.shrink(drainedFluid.getAmount());
                    }
                }
                blockEntity.sync(level);
            }
        }

        public IFoundryFuel getFoundryFuel(Level level, FoundryControllerBlockEntity blockEntity) {
            return new FluidFuel(blockEntity.getFuel());
        }
    }

    static class EnergyMelter implements IMelterProcessor
    {
        public void tick(Level level, BlockPos pos, BlockState state, FoundryControllerBlockEntity blockEntity) {
            var mb = blockEntity.getMultiblockData();

            // Melt items from inventory if they have a melting recipe
            int power = blockEntity.getPower();
            if (power == 0) {
                return;
            }

            // Get heating coil information
            var coilPos = new BlockPos(
                    (blockEntity.getMultiblockData().topCorners().getFirst().getX() + blockEntity.getMultiblockData().topCorners().getSecond().getX()) / 2,
                    blockEntity.getMultiblockData().topCorners().getFirst().getY() - blockEntity.getMultiblockData().height(),
                    (blockEntity.getMultiblockData().topCorners().getFirst().getZ() + blockEntity.getMultiblockData().topCorners().getSecond().getZ()) / 2
            );

            IFoundryFuel powerFuel = new PowerFuel(power, level.getBlockState(coilPos).getBlock());
            if (powerFuel.getFuelData() == null) {
                return;
            }

            ProductiveMetalworks.LOGGER.info("coil data at " + coilPos + " " + powerFuel.getFuelData());

            // Create an accumulator to count how much fuel will be used
            IFoundryFuel consumedFuel = new PowerFuel(0, powerFuel.getFuelData());

            boolean hasChanged = meltItems(level, blockEntity, consumedFuel, powerFuel);

            if (hasChanged) {
                for (BlockPos blockPos : mb.peripherals()) {
                    if (!consumedFuel.isEmpty() && level.getBlockEntity(blockPos) instanceof FoundryCapacitorBlockEntity capacitorBlockEntity) {
                        int drainedPower = capacitorBlockEntity.energyHandler.extractEnergy(consumedFuel.getAmount(), false);
                        consumedFuel.shrink(drainedPower);
                    }
                }
                blockEntity.sync(level);
            }
        }

        public IFoundryFuel getFoundryFuel(Level level, FoundryControllerBlockEntity blockEntity) {
            var coilPos = new BlockPos(
                    (blockEntity.getMultiblockData().topCorners().getFirst().getX() + blockEntity.getMultiblockData().topCorners().getSecond().getX()) / 2,
                    blockEntity.getMultiblockData().topCorners().getFirst().getY() - blockEntity.getMultiblockData().height(),
                    (blockEntity.getMultiblockData().topCorners().getFirst().getZ() + blockEntity.getMultiblockData().topCorners().getSecond().getZ()) / 2
            );

            return new PowerFuel(blockEntity.getPower(), level.getBlockState(coilPos).getBlock());
        }
    }

    static class FluidFuel implements IFoundryFuel
    {
        private final FluidStack fluidStack;
        private FuelMap fuelData;

        FluidFuel(FluidStack fluidStack) {
            this.fluidStack = fluidStack;
            this.fuelData = fluidStack.getFluid().builtInRegistryHolder().getData(MetalworksRegistrator.FUEL_MAP);
        }

        @Override
        public FuelMap getFuelData() {
            return fuelData;
        }

        @Override
        public int getAmount() {
            return fluidStack.getAmount();
        }

        @Override
        public void grow(int amount) {
            fluidStack.grow(amount);
        }

        @Override
        public void shrink(int amount) {
            fluidStack.shrink(amount);
        }

        @Override
        public boolean isEmpty() {
            return fluidStack.isEmpty();
        }
    }

    static class PowerFuel implements IFoundryFuel
    {
        private int power;
        private FuelMap fuelData;

        PowerFuel(int power, Block coilBlock) {
            this.power = power;
            this.fuelData = coilBlock.builtInRegistryHolder().getData(MetalworksRegistrator.POWER_COIL_MAP);
        }

        PowerFuel(int power, FuelMap fuelData) {
            this.power = power;
            this.fuelData = fuelData;
        }

        @Override
        public FuelMap getFuelData() {
            return fuelData;
        }

        @Override
        public int getAmount() {
            return power;
        }

        @Override
        public void grow(int amount) {
            power += amount;
        }

        @Override
        public void shrink(int amount) {
            power -= amount;
        }

        @Override
        public boolean isEmpty() {
            return power == 0;
        }
    }
}

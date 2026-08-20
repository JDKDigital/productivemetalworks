package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.CapabilityBlockEntity;
import cy.jdkdigital.productivelib.common.block.entity.InventoryHandlerHelper;
import cy.jdkdigital.productivemetalworks.Config;
import cy.jdkdigital.productivemetalworks.recipe.BlockCastingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.ItemCastingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import cy.jdkdigital.productivemetalworks.util.ModFluidTank;
import cy.jdkdigital.productivemetalworks.util.RecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.Optional;
import net.minecraft.world.item.ItemStackTemplate;

public class CastingBlockEntity extends CapabilityBlockEntity
{
    public int coolingTime = 0;
    public int maxAmount = 1000;

    // cast inventory, no cap
    public InventoryHandlerHelper.BlockEntityItemStackHandler castInv = new InventoryHandlerHelper.BlockEntityItemStackHandler(1, this) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack, boolean fromAutomation) {
            // The single slot is a dedicated cast slot and accepts any item; the generic handler would
            // reject it because slot 0 == BOTTLE_SLOT isn't in its insertable set.
            return true;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean fromAutomation) {
            if (CastingBlockEntity.this.isCooling() || CastingBlockEntity.this.fluidHandler.getFluidAmount() > 0) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate, fromAutomation);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            super.onContentsChanged(index, previousContents);
            if (CastingBlockEntity.this.level instanceof ServerLevel serverLevel) {
                CastingBlockEntity.this.sync(serverLevel);
            }
            CastingBlockEntity.this.setChanged();
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            return 1;
        }
    };

    // result item inventory, with cap
    InventoryHandlerHelper.BlockEntityItemStackHandler itemHandler = new InventoryHandlerHelper.BlockEntityItemStackHandler(1, this) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack, boolean fromAutomation) {
            // Never insertable — the result is written internally by serverTick; only castInv accepts items.
            return false;
        }

        @Override
        public boolean isInputSlot(int slot) {
            // Slot 0 is the result, not an input; allow a hopper under the block to pull it.
            return false;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (CastingBlockEntity.this.isCooling() || CastingBlockEntity.this.fluidHandler.getFluidAmount() > 0) {
                return 0;
            }
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            super.onContentsChanged(index, previousContents);
            if (CastingBlockEntity.this.level instanceof ServerLevel serverLevel) {
                CastingBlockEntity.this.sync(serverLevel);
            }
            CastingBlockEntity.this.setChanged();
        }
    };

    // fluid inv for casting fluid
    ModFluidTank fluidHandler = new ModFluidTank(1000) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            if (CastingBlockEntity.this.level == null || CastingBlockEntity.this.isCooling()) {
                return false;
            }
            // PORT-TODO (26.1): the old "cast is a fluid container" fast-path used
            // ItemStack#getCapability(Capabilities.Fluid.ITEM); the 26.1 item fluid
            // capability now requires an ItemAccess context. Buckets are handled in serverTick.
            // Valid if the cast + fluid has a recipe and there's not enough fluid to fulfill the recipe
            var recipe = CastingBlockEntity.this.findRecipe(CastingBlockEntity.this.level, CastingBlockEntity.this.castInv.getStackInSlot(0), stack);
            return recipe != null && this.getFluidAmount() < recipe.getFluidAmount(CastingBlockEntity.this.level, stack) && CastingBlockEntity.this.itemHandler.getStackInSlot(0).isEmpty();
        }

        // The tap/automation pour uses ResourceHandler#insert, not the legacy fill below, so the recipe cap
        // must be enforced here too (getCapacity bounds the slot to the recipe amount, preventing overshoot).
        @Override
        public boolean isValid(int index, FluidResource resource) {
            if (CastingBlockEntity.this.level == null || CastingBlockEntity.this.isCooling()
                    || !CastingBlockEntity.this.itemHandler.getStackInSlot(0).isEmpty()) {
                return false;
            }
            return CastingBlockEntity.this.findRecipe(CastingBlockEntity.this.level, CastingBlockEntity.this.castInv.getStackInSlot(0), resource.toStack(1)) != null;
        }

        @Override
        protected int getCapacity(int index, FluidResource resource) {
            int base = super.getCapacity(index, resource);
            if (CastingBlockEntity.this.level == null) {
                return base;
            }
            var recipe = CastingBlockEntity.this.findRecipe(CastingBlockEntity.this.level, CastingBlockEntity.this.castInv.getStackInSlot(0), resource.toStack(1));
            return recipe == null ? 0 : Math.min(base, recipe.getFluidAmount(CastingBlockEntity.this.level, resource.toStack(1)));
        }

        @Override
        public int fill(FluidStack resource, boolean execute) {
            if (resource.isEmpty() || !isFluidValid(resource) || CastingBlockEntity.this.level == null) {
                return 0;
            }
            var recipe = CastingBlockEntity.this.findRecipe(CastingBlockEntity.this.level, CastingBlockEntity.this.castInv.getStackInSlot(0), resource);
            if (recipe == null) {
                return 0;
            }
            FluidStack current = getFluid();
            if (!execute) {
                if (current.isEmpty()) {
                    return Math.min(recipe.getFluidAmount(CastingBlockEntity.this.level, resource), resource.getAmount());
                }
                if (!FluidStack.isSameFluidSameComponents(current, resource)) {
                    return 0;
                }
                return Math.min(recipe.getFluidAmount(CastingBlockEntity.this.level, resource) - current.getAmount(), resource.getAmount());
            }
            CastingBlockEntity.this.maxAmount = recipe.getFluidAmount(level, current);
            if (current.isEmpty()) {
                FluidStack stored = resource.copyWithAmount(Math.min(recipe.getFluidAmount(CastingBlockEntity.this.level, resource), resource.getAmount()));
                setFluid(stored);
                return stored.getAmount();
            }
            if (!FluidStack.isSameFluidSameComponents(current, resource)) {
                return 0;
            }
            int filled = recipe.getFluidAmount(CastingBlockEntity.this.level, resource) - current.getAmount();
            FluidStack updated = current.copy();
            if (resource.getAmount() < filled) {
                updated.grow(resource.getAmount());
                filled = resource.getAmount();
            } else {
                updated.setAmount(recipe.getFluidAmount(CastingBlockEntity.this.level, resource));
            }
            if (filled > 0) {
                setFluid(updated);
            }
            return filled;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (CastingBlockEntity.this.isCooling()) {
                return 0;
            }
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean execute) {
            if (CastingBlockEntity.this.isCooling()) {
                return FluidStack.EMPTY;
            }
            return super.drain(maxDrain, execute);
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean execute) {
            if (CastingBlockEntity.this.isCooling()) {
                return FluidStack.EMPTY;
            }
            return super.drain(resource, execute);
        }

        @Override
        protected void onContentsChanged() {
            if (CastingBlockEntity.this.level instanceof ServerLevel serverLevel) {
                CastingBlockEntity.this.sync(serverLevel);
            }
            CastingBlockEntity.this.setChanged();
        }
    };

    public CastingBlockEntity(BlockPos pos, BlockState blockState) {
        super(MetalworksRegistrator.CASTING_BLOCK_ENTITY.get(), pos, blockState);
    }

    public boolean isCooling() {
        return this.coolingTime > 0;
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, CastingBlockEntity castingTableBlock) {
        if (level instanceof ServerLevel serverLevel) {
            if (castingTableBlock.isCooling()) {
                castingTableBlock.coolingTime--;
                if (castingTableBlock.coolingTime == 0) {
                    // Finalize casting
                    var fluid = castingTableBlock.getFluidHandler().getFluid();
                    var recipe = castingTableBlock.findRecipe(level, castingTableBlock.castInv.getStackInSlot(0), fluid);
                    if (recipe != null && fluid.getAmount() >= recipe.getFluidAmount(level, fluid)) {
                        castingTableBlock.getFluidHandler().drain(recipe.getFluidAmount(level, fluid), true);
                        if (recipe.consumeCast) {
                            if (recipe.result.create().is(ModTags.Items.CASTS)) {
                                castingTableBlock.castInv.setStackInSlot(0, recipe.result.create());
                                var remainingResult = castingTableBlock.itemHandler.getStackInSlot(0);
                                remainingResult.shrink(1);
                                castingTableBlock.itemHandler.setStackInSlot(0, remainingResult.isEmpty() ? ItemStack.EMPTY : remainingResult);
                            } else {
                                castingTableBlock.castInv.setStackInSlot(0, ItemStack.EMPTY);
                            }
                        }
                        castingTableBlock.sync(serverLevel);
                        level.playSound(null, blockPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, level.getRandom().nextInt(50, 100)/100f, level.getRandom().nextInt(80, 100)/100f);
                    }
                }
            }

            // Initiate cooling
            if (castingTableBlock.coolingTime == 0 && castingTableBlock.itemHandler.getStackInSlot(0).isEmpty() && castingTableBlock.getFluidHandler().getFluidAmount() > 0) {
                var fluid = castingTableBlock.getFluidHandler().getFluid();
                // First check if the "cast" is a fluid handler that can be filled with the fluid
                boolean hasFilledContainer = false;
                var cast = castingTableBlock.castInv.getStackInSlot(0);
                if (cast.is(Items.BUCKET) && fluid.getAmount() >= FluidType.BUCKET_VOLUME) {
                    var filledBucket = FluidUtil.getFilledBucket(fluid);
                    if (!filledBucket.isEmpty()) {
                        castingTableBlock.getFluidHandler().drain(FluidType.BUCKET_VOLUME, true);
                        castingTableBlock.itemHandler.setStackInSlot(0, filledBucket);
                        castingTableBlock.castInv.setStackInSlot(0, ItemStack.EMPTY);
                        castingTableBlock.sync(serverLevel);
                        hasFilledContainer = true;
                    }
                }
                // else check for a recipe
                if (!hasFilledContainer) {
                    var recipe = castingTableBlock.findRecipe(level, cast, fluid);
                    if (recipe != null && fluid.getAmount() >= recipe.getFluidAmount(level, fluid)) {
                        // insert item but disable pulling and picking the item when coolingTime > 0
                        var resultItem = recipe.getResultItem(level, fluid);
                        castingTableBlock.itemHandler.setStackInSlot(0, resultItem);
                        castingTableBlock.coolingTime = Math.max(1, (int) (recipe.getFluidAmount(level, fluid) / Config.foundryCoolingModifier));
                        castingTableBlock.maxAmount = recipe.getFluidAmount(level, fluid);
                        castingTableBlock.sync(serverLevel);
                    }
                }
            }
        }
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, CastingBlockEntity castingTableBlock) {
        if (castingTableBlock.isCooling()) {
            castingTableBlock.coolingTime--;
        }
    }

    @Override
    public void savePacketNBT(ValueOutput output) {
        super.savePacketNBT(output);

        output.putChild("cast", this.castInv);
        output.putInt("maxAmount", this.maxAmount);
        output.putInt("coolingTime", this.coolingTime);
    }

    @Override
    public void loadPacketNBT(ValueInput input) {
        super.loadPacketNBT(input);

        input.readChild("cast", this.castInv);
        this.coolingTime = input.getIntOr("coolingTime", 0);
        this.maxAmount = input.getIntOr("maxAmount", this.maxAmount);
    }

    /** Output item in the result slot (empty if none). */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (!isCooling()) {
            super.preRemoveSideEffects(pos, state);
        }
        if (this.level != null) {
            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), castInv.getStackInSlot(0));
        }
    }

    public ItemStack getResultStack() {
        return itemHandler.getStackInSlot(0);
    }

    /** Remove the result item if present, otherwise the cast. Used when a player takes the output. */
    public void clearResultOrCast() {
        if (!itemHandler.getStackInSlot(0).isEmpty()) {
            itemHandler.setStackInSlot(0, ItemStack.EMPTY);
        } else {
            castInv.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public boolean canAcceptCast() {
        return itemHandler.getStackInSlot(0).isEmpty() && // no crafted output
               castInv.getStackInSlot(0).isEmpty() && // no cast
               getFluidHandler().getFluidAmount() == 0; // no fluid
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandler() {
        return itemHandler;
    }

    @Override
    public ModFluidTank getFluidHandler() {
        return fluidHandler;
    }

    public void sync(ServerLevel serverLevel) {
        this.invalidateCapabilities();
        serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private ItemCastingRecipe findRecipe(Level level, ItemStack cast, FluidStack fluid) {
        boolean isTable = getBlockState().is(MetalworksRegistrator.CASTING_TABLE.get());
        // Bucket filling
        if (isTable && cast.is(Items.BUCKET)) {
            var filledBucket = FluidUtil.getFilledBucket(fluid);
            if (!filledBucket.isEmpty()) {
                return new ItemCastingRecipe(Optional.of(Ingredient.of(cast.getItem())), new SizedFluidIngredient(FluidIngredient.of(fluid), FluidType.BUCKET_VOLUME), ItemStackTemplate.fromNonEmptyStack(filledBucket), true);
            }
        }
        // Waxing
        if (!isTable && fluid.is(MetalworksRegistrator.MOLTEN_WAX.get()) && cast.getItem() instanceof BlockItem block) {
            var waxData = block.getBlock().builtInRegistryHolder().getData(NeoForgeDataMaps.WAXABLES);
            if (waxData != null) {
                return new BlockCastingRecipe(Optional.of(Ingredient.of(cast.getItem())), new SizedFluidIngredient(FluidIngredient.of(fluid), 50), new ItemStackTemplate(waxData.waxed().asItem()), true);
            }
        }

        // event for getting recipe
        ItemCastingRecipe compatRecipe = RecipeHelper.getCompatRecipe(level, cast, fluid, isTable);
        if (compatRecipe != null) {
            return compatRecipe;
        }

        // regular item and block casting recipes
        if (isTable) {
            var recipe = RecipeHelper.getItemCastingRecipe(level, cast, fluid);
            return recipe == null ? null : recipe.value();
        }
        var recipe = RecipeHelper.getBlockCastingRecipe(level, cast, fluid);
        return recipe == null ? null : recipe.value();
    }
}

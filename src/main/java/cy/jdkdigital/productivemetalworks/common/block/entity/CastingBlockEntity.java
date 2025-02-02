package cy.jdkdigital.productivemetalworks.common.block.entity;

import cy.jdkdigital.productivelib.common.block.entity.CapabilityBlockEntity;
import cy.jdkdigital.productivemetalworks.Config;
import cy.jdkdigital.productivemetalworks.recipe.BlockCastingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.ItemCastingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import cy.jdkdigital.productivemetalworks.util.RecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.NotNull;

public class CastingBlockEntity extends CapabilityBlockEntity
{
    public int coolingTime = 0;
    public int maxAmount = 1000;

    // cast inventory, no cap
    public ItemStackHandler castInv = new ItemStackHandler(1);

    // result item inventory, with cap
    IItemHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (CastingBlockEntity.this.level == null || CastingBlockEntity.this.isCooling()) {
                return false;
            }
            if (stack.getCapability(Capabilities.FluidHandler.ITEM) != null) {
                return true;
            }

            var recipe = CastingBlockEntity.this.findRecipe(CastingBlockEntity.this.level, CastingBlockEntity.this.castInv.getStackInSlot(0), CastingBlockEntity.this.fluidHandler.getFluid());
            return recipe != null && ItemStack.isSameItemSameComponents(recipe.getResultItem(CastingBlockEntity.this.level, CastingBlockEntity.this.fluidHandler.getFluid()), stack); // only insert items matching the current recipe
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (CastingBlockEntity.this.isCooling()) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (CastingBlockEntity.this.level instanceof ServerLevel serverLevel) {
                CastingBlockEntity.this.sync(serverLevel);
            }
            CastingBlockEntity.this.setChanged();
        }
    };

    // fluid inv for casting fluid
    FluidTank fluidHandler = new FluidTank(1000) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            if (CastingBlockEntity.this.level == null || CastingBlockEntity.this.isCooling()) {
                return false;
            }
            // Valid if the cast is a fluid container
            if (CastingBlockEntity.this.castInv.getStackInSlot(0).getCapability(Capabilities.FluidHandler.ITEM) != null) {
                return true;
            }
            // Valid if the cast + fluid has a recipe and there's not enough fluid to fulfill the recipe
            var recipe = CastingBlockEntity.this.findRecipe(CastingBlockEntity.this.level, CastingBlockEntity.this.castInv.getStackInSlot(0), stack);
            return recipe != null && this.getFluidAmount() < recipe.getFluidAmount(CastingBlockEntity.this.level, stack) && CastingBlockEntity.this.getItemHandler().getStackInSlot(0).isEmpty();
        }

        @Override
        public int fill(FluidStack resource, @NotNull FluidAction action) {
            if (resource.isEmpty() || !isFluidValid(resource) || CastingBlockEntity.this.level == null) {
                return 0;
            }
            var recipe = CastingBlockEntity.this.findRecipe(CastingBlockEntity.this.level, CastingBlockEntity.this.castInv.getStackInSlot(0), resource);
            if (recipe == null) {
                return 0;
            }
            if (action.simulate()) {
                if (fluid.isEmpty()) {
                    return Math.min(recipe.getFluidAmount(CastingBlockEntity.this.level, resource), resource.getAmount());
                }
                if (!FluidStack.isSameFluidSameComponents(fluid, resource)) {
                    return 0;
                }
                return Math.min(recipe.getFluidAmount(CastingBlockEntity.this.level, resource) - fluid.getAmount(), resource.getAmount());
            }
            CastingBlockEntity.this.maxAmount = recipe.getFluidAmount(level, fluid);
            if (fluid.isEmpty()) {
                fluid = resource.copyWithAmount(Math.min(recipe.getFluidAmount(CastingBlockEntity.this.level, resource), resource.getAmount()));
                onContentsChanged();
                return fluid.getAmount();
            }
            if (!FluidStack.isSameFluidSameComponents(fluid, resource)) {
                return 0;
            }
            int filled = recipe.getFluidAmount(CastingBlockEntity.this.level, resource) - fluid.getAmount();

            if (resource.getAmount() < filled) {
                fluid.grow(resource.getAmount());
                filled = resource.getAmount();
            } else {
                fluid.setAmount(recipe.getFluidAmount(CastingBlockEntity.this.level, resource));
            }
            if (filled > 0) {
                onContentsChanged();
            }
            return filled;
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
                        fluid.shrink(recipe.getFluidAmount(level, fluid));
                        if (recipe.consumeCast) {
                            if (recipe.result.is(ModTags.Items.CASTS)) {
                                castingTableBlock.castInv.setStackInSlot(0, recipe.result.copy());
                                castingTableBlock.getItemHandler().getStackInSlot(0).shrink(1);
                            } else {
                                castingTableBlock.castInv.setStackInSlot(0, ItemStack.EMPTY);
                            }
                        }
                        castingTableBlock.sync(serverLevel);
                        level.playSound(null, blockPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, level.random.nextInt(50, 100)/100f, level.random.nextInt(80, 100)/100f);
                    }
                }
            }

            // Initiate cooling
            if (castingTableBlock.coolingTime == 0 && castingTableBlock.getItemHandler().getStackInSlot(0).isEmpty() && castingTableBlock.getFluidHandler().getFluidAmount() > 0) {
                var fluid = castingTableBlock.getFluidHandler().getFluid();
                // First check if the "cast" is a fluid handler that can be filled with the fluid
                boolean hasFilledContainer = false;
                var cast = castingTableBlock.castInv.getStackInSlot(0);
                if (cast.is(Items.BUCKET)) {
                    var fillResult = FluidUtil.tryFillContainer(cast, castingTableBlock.getFluidHandler(), fluid.getAmount(), null, true);
                    if (fillResult.isSuccess()) {
                        castingTableBlock.getItemHandler().insertItem(0, fillResult.getResult(), false);
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
                        castingTableBlock.getItemHandler().insertItem(0, resultItem, false);
                        castingTableBlock.coolingTime = (int) (recipe.getFluidAmount(level, fluid) / Config.foundryCoolingModifier);
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
    public void savePacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.savePacketNBT(tag, provider);

        tag.put("cast", this.castInv.serializeNBT(provider));
        tag.putInt("maxAmount", this.maxAmount);
        tag.putInt("coolingTime", this.coolingTime);
    }

    @Override
    public void loadPacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadPacketNBT(tag, provider);

        if (tag.contains("cast")) {
            this.castInv.deserializeNBT(provider, tag.getCompound("cast"));
        }
        if (tag.contains("coolingTime")) {
            this.coolingTime = tag.getInt("coolingTime");
        }
        if (tag.contains("maxAmount")) {
            this.maxAmount = tag.getInt("maxAmount");
        }
    }

    @Override
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public FluidTank getFluidHandler() {
        return fluidHandler;
    }

    public void sync(ServerLevel serverLevel) {
        serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private ItemCastingRecipe findRecipe(Level level, ItemStack cast, FluidStack fluid) {
        boolean isTable = getBlockState().is(MetalworksRegistrator.CASTING_TABLE.get());
        // Bucket filling
        if (isTable && cast.is(Items.BUCKET)) {
            return new ItemCastingRecipe(Ingredient.of(cast), new SizedFluidIngredient(FluidIngredient.single(fluid), FluidType.BUCKET_VOLUME), FluidUtil.getFilledBucket(fluid), true);
        }
        // Waxing
        if (!isTable && fluid.is(MetalworksRegistrator.MOLTEN_WAX.get()) && cast.getItem() instanceof BlockItem block) {
            var waxData = block.getBlock().builtInRegistryHolder().getData(NeoForgeDataMaps.WAXABLES);
            if (waxData != null) {
                return new BlockCastingRecipe(Ingredient.of(cast), new SizedFluidIngredient(FluidIngredient.single(fluid), 50), waxData.waxed().asItem().getDefaultInstance(), true);
            }
        }
        if (isTable) {
            if (ModList.get().isLoaded("silentgear") && (cast.is(ModTags.Items.SG_BLUEPRINTS) || cast.is(Items.HEAVY_CORE))) {
                var recipe = RecipeHelper.getSilentGearCastingRecipe(level, cast, fluid);
                if (recipe != null) {
                    return recipe.value();
                }
            }
            var recipe = RecipeHelper.getItemCastingRecipe(level, cast, fluid);
            return recipe == null ? null : recipe.value();
        }
        var recipe = RecipeHelper.getBlockCastingRecipe(level, cast, fluid);
        return recipe == null ? null : recipe.value();
    }
}

package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.FluidHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.silentchaos512.gear.crafting.ingredient.PartMaterialIngredient;
import net.silentchaos512.gear.gear.material.MaterialInstance;
import net.silentchaos512.gear.item.CompoundPartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SilentGearCastingRecipe extends ItemCastingRecipe
{
    public final PartMaterialIngredient material;
    public final int materialCount;

    public SilentGearCastingRecipe(Ingredient cast, PartMaterialIngredient material, int materialCount, ItemStack result, boolean consumeCast) {
        super(cast, new SizedFluidIngredient(FluidIngredient.of(Fluids.WATER), 1), result, consumeCast);
        this.material = material;
        this.materialCount = materialCount;
    }

    @Override
    public ItemStack getResultItem(Level level, FluidStack containedFluid) {
        List<MaterialInstance> mats = new ArrayList<>();
        var allowedFluids = FluidHelper.materialsToFluids(level, material.getItems(), materialCount);
        for (Map.Entry<Fluid, Pair<ItemStack, Integer>> entry : allowedFluids.entrySet()) {
            if (containedFluid.is(entry.getKey())) {
                var materialStack = entry.getValue().getFirst();
                for (int i = 0; i < materialCount; i++) {
                    mats.add(MaterialInstance.from(materialStack));
                }
            }
        }
        if (this.result.getItem() instanceof CompoundPartItem partItem) {
            return partItem.create(mats);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(ItemStack cast, FluidStack fluid, Level level) {
        return matches(cast, fluid, false, level);
    }

    @Override
    public boolean matches(ItemStack cast, FluidStack fluid, boolean matchFluidAmount, Level level) {
        if (!this.cast.test(cast)) {
            return false;
        }
        // Test if input fluid is in the allowed fluid values
        var allowedFluids = FluidHelper.materialsToFluids(level, material.getItems(), materialCount);
        for (Fluid entry : allowedFluids.keySet()) {
            if (fluid.is(entry)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getFluidAmount(Level level, FluidStack containedFluid) {
        var allowedFluids = FluidHelper.materialsToFluids(level, material.getItems(), materialCount);
        if (allowedFluids.containsKey(containedFluid.getFluid())) {
            return allowedFluids.get(containedFluid.getFluid()).getSecond();
        }
        return containedFluid.getAmount();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MetalworksRegistrator.SG_GEAR_CASTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MetalworksRegistrator.SG_GEAR_CASTING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<SilentGearCastingRecipe>
    {
        private static final MapCodec<SilentGearCastingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                Ingredient.CODEC.fieldOf("cast").orElse(Ingredient.EMPTY).forGetter(recipe -> recipe.cast),
                                PartMaterialIngredient.CODEC.fieldOf("material").forGetter(recipe -> recipe.material),
                                Codec.INT.fieldOf("material_count").orElse(1).forGetter(recipe -> recipe.materialCount),
                                ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                                Codec.BOOL.fieldOf("consume_cast").orElse(false).forGetter(recipe -> recipe.consumeCast)
                        )
                        .apply(builder, SilentGearCastingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, SilentGearCastingRecipe> STREAM_CODEC = StreamCodec.of(
                SilentGearCastingRecipe.Serializer::toNetwork, SilentGearCastingRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<SilentGearCastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SilentGearCastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static SilentGearCastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            return new SilentGearCastingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), PartMaterialIngredient.STREAM_CODEC.decode(buffer), buffer.readInt(), ItemStack.STREAM_CODEC.decode(buffer), buffer.readBoolean());
        }

        public static void toNetwork(RegistryFriendlyByteBuf buffer, SilentGearCastingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.cast);
            PartMaterialIngredient.STREAM_CODEC.encode(buffer, recipe.material);
            buffer.writeInt(recipe.materialCount);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeBoolean(recipe.consumeCast);
        }
    }
}

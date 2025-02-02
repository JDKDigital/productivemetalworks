package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public class FluidAlloyingRecipe implements Recipe<RecipeInput>
{
    public final List<SizedFluidIngredient> fluids;
    public final int speed;
    public final FluidStack result;

    public FluidAlloyingRecipe(List<SizedFluidIngredient> fluids, int speed, FluidStack result) {
        this.fluids = fluids;
        this.speed = speed;
        this.result = result;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return false;
    }

    public boolean matches(List<FluidStack> availableFluids) {
        return this.fluids.stream().allMatch(sizedFluidIngredient -> {
            boolean hasMatch = false;
            for (FluidStack fluidStack : availableFluids) {
                if (sizedFluidIngredient.test(fluidStack)) {
                    hasMatch = true;
                    break;
                }
            }
            return hasMatch;
        });

    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MetalworksRegistrator.FLUID_ALLOYING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MetalworksRegistrator.FLUID_ALLOYING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<FluidAlloyingRecipe>
    {
        private static final MapCodec<FluidAlloyingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                SizedFluidIngredient.FLAT_CODEC.listOf().fieldOf("fluids").forGetter(recipe -> recipe.fluids),
                                Codec.INT.fieldOf("speed").orElse(1).forGetter(recipe -> recipe.speed),
                                FluidStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                        )
                        .apply(builder, FluidAlloyingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidAlloyingRecipe> STREAM_CODEC = StreamCodec.of(
                FluidAlloyingRecipe.Serializer::toNetwork, FluidAlloyingRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<FluidAlloyingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluidAlloyingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static FluidAlloyingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            return new FluidAlloyingRecipe(SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer), buffer.readInt(), FluidStack.STREAM_CODEC.decode(buffer));
        }

        public static void toNetwork(RegistryFriendlyByteBuf buffer, FluidAlloyingRecipe recipe) {
            SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.fluids);
            buffer.writeInt(recipe.speed);
            FluidStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}

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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class ItemMeltingRecipe implements Recipe<RecipeInput>
{
    public final Ingredient item;
    public final List<FluidStack> result;
    private final int minTemperature;
    private final int maxTemperature;

    public ItemMeltingRecipe(Ingredient item, List<FluidStack> result, int minTemperature, int maxTemperature) {
        this.item = item;
        this.result = result;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return false;
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
        return MetalworksRegistrator.ITEM_MELTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MetalworksRegistrator.ITEM_MELTING_TYPE.get();
    }

    public boolean matches(ItemStack input) {
        return this.item.test(input);
    }

    public static class Serializer implements RecipeSerializer<ItemMeltingRecipe>
    {
        private static final MapCodec<ItemMeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(recipe -> recipe.item),
                                FluidStack.CODEC.listOf().fieldOf("result").forGetter(recipe -> recipe.result),
                                Codec.INT.fieldOf("minimum_temperature").forGetter(recipe -> recipe.minTemperature),
                                Codec.INT.fieldOf("maximum_temperature").orElse(0).forGetter(recipe -> recipe.maxTemperature)
                        )
                        .apply(builder, ItemMeltingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemMeltingRecipe> STREAM_CODEC = StreamCodec.of(
                ItemMeltingRecipe.Serializer::toNetwork, ItemMeltingRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<ItemMeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ItemMeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static ItemMeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            return new ItemMeltingRecipe(
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                    FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
                    buffer.readInt(),
                    buffer.readInt()
            );
        }

        public static void toNetwork(RegistryFriendlyByteBuf buffer, ItemMeltingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.item);
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.result);
            buffer.writeInt(recipe.minTemperature);
            buffer.writeInt(recipe.maxTemperature);
        }
    }
}

package cy.jdkdigital.productivemetalworks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.FluidStackTemplate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class ItemMeltingRecipe implements Recipe<RecipeInput>
{
    public static final MapCodec<ItemMeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.item),
                            FluidStackTemplate.CODEC.listOf().fieldOf("result").forGetter(recipe -> recipe.result),
                            Codec.INT.fieldOf("minimum_temperature").forGetter(recipe -> recipe.minTemperature),
                            Codec.INT.fieldOf("maximum_temperature").orElse(0).forGetter(recipe -> recipe.maxTemperature)
                    )
                    .apply(builder, ItemMeltingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemMeltingRecipe> STREAM_CODEC = StreamCodec.of(
            ItemMeltingRecipe::toNetwork, ItemMeltingRecipe::fromNetwork
    );

    public static final RecipeSerializer<ItemMeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public final Ingredient item;
    public final List<FluidStackTemplate> result;
    public final int minTemperature;
    public final int maxTemperature;

    public ItemMeltingRecipe(Ingredient item, List<FluidStackTemplate> result, int minTemperature, int maxTemperature) {
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
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<ItemMeltingRecipe> getSerializer() {
        return MetalworksRegistrator.ITEM_MELTING.get();
    }

    @Override
    public RecipeType<ItemMeltingRecipe> getType() {
        return MetalworksRegistrator.ITEM_MELTING_TYPE.get();
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public boolean matches(ItemStack input, int fuelTemperature) {
        return this.item.test(input) && minTemperature <= fuelTemperature;
    }

    public static ItemMeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new ItemMeltingRecipe(
                Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                FluidStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
                buffer.readInt(),
                buffer.readInt()
        );
    }

    public static void toNetwork(RegistryFriendlyByteBuf buffer, ItemMeltingRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.item);
        FluidStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.result);
        buffer.writeInt(recipe.minTemperature);
        buffer.writeInt(recipe.maxTemperature);
    }
}

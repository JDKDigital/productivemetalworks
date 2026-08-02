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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public class FluidAlloyingRecipe implements Recipe<RecipeInput>
{
    public static final MapCodec<FluidAlloyingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            SizedFluidIngredient.CODEC.listOf().fieldOf("fluids").forGetter(recipe -> recipe.fluids),
                            Codec.INT.fieldOf("speed").orElse(1).forGetter(recipe -> recipe.speed),
                            FluidStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                    )
                    .apply(builder, FluidAlloyingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidAlloyingRecipe> STREAM_CODEC = StreamCodec.of(
            FluidAlloyingRecipe::toNetwork, FluidAlloyingRecipe::fromNetwork
    );

    public static final RecipeSerializer<FluidAlloyingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public final List<SizedFluidIngredient> fluids;
    public final int speed;
    public final FluidStackTemplate result;

    public FluidAlloyingRecipe(List<SizedFluidIngredient> fluids, int speed, FluidStackTemplate result) {
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
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<FluidAlloyingRecipe> getSerializer() {
        return MetalworksRegistrator.FLUID_ALLOYING.get();
    }

    @Override
    public RecipeType<FluidAlloyingRecipe> getType() {
        return MetalworksRegistrator.FLUID_ALLOYING_TYPE.get();
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

    public static FluidAlloyingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new FluidAlloyingRecipe(SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer), buffer.readInt(), FluidStackTemplate.STREAM_CODEC.decode(buffer));
    }

    public static void toNetwork(RegistryFriendlyByteBuf buffer, FluidAlloyingRecipe recipe) {
        SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.fluids);
        buffer.writeInt(recipe.speed);
        FluidStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
    }
}

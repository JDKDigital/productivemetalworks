package cy.jdkdigital.productivemetalworks.integration.jei;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.recipe.FluidAlloyingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.ItemMeltingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;
import java.util.Comparator;

public class FluidAlloyingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<FluidAlloyingRecipe>>
{
    private final IDrawable background;

    public FluidAlloyingRecipeCategory(IGuiHelper guiHelper) {
        super(
                JeiPlugin.FLUID_ALLOYING,
                Component.translatable("jei." + ProductiveMetalworks.MODID + ".fluid_alloying"),
                guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MetalworksRegistrator.FOUNDRY_DRAINS.get(DyeColor.BLACK).get())),
                165, 68
        );
        this.background = guiHelper.drawableBuilder(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "textures/gui/jei/fluid_alloying.png"), 0, 0, 165, 68).setTextureSize(165, 68).build();
    }

    @Override
    public void draw(RecipeHolder<FluidAlloyingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics, 0, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<FluidAlloyingRecipe> recipe, IFocusGroup focuses) {
        int maxAmount = Math.max(recipe.value().result.getAmount(), recipe.value().fluids.stream().map(SizedFluidIngredient::amount).max(Integer::compareTo).get());

        int fWidth = 42/recipe.value().fluids.size();
        for (int i = 0, fluidsSize = recipe.value().fluids.size(); i < fluidsSize; i++) {
            SizedFluidIngredient sizedFluidIngredient = recipe.value().fluids.get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, 12 + (i*fWidth), 8)
                    .addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.stream(sizedFluidIngredient.getFluids()).filter(fluidStack -> fluidStack.getFluid().defaultFluidState().isSource()).toList())
                    .setFluidRenderer(maxAmount, false, fWidth,52)
                    .setSlotName("fluid" + i);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 8)
                .addFluidStack(recipe.value().result.getFluid(), recipe.value().result.getAmount())
                .setFluidRenderer(maxAmount, false, 42,52)
                .setSlotName("result");

    }
}

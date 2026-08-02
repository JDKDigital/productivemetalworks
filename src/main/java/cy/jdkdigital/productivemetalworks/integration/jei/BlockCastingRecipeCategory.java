package cy.jdkdigital.productivemetalworks.integration.jei;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.recipe.BlockCastingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.FluidHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class BlockCastingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<BlockCastingRecipe>>
{
    private final IDrawable background;

    public BlockCastingRecipeCategory(IGuiHelper guiHelper) {
        super(
                JeiPlugin.BLOCK_CASTING,
                Component.translatable("jei." + ProductiveMetalworks.MODID + ".block_casting"),
                guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MetalworksRegistrator.CASTING_BASIN.get())),
                165, 68
        );
        this.background = guiHelper.drawableBuilder(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "textures/gui/jei/block_casting.png"), 0, 0, 165, 68).setTextureSize(165, 68).build();
    }

    @Override
    public void draw(RecipeHolder<BlockCastingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics, 0, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BlockCastingRecipe> recipe, IFocusGroup focuses) {
        var hasCast = recipe.value().cast.isPresent();
        if (hasCast) {
            builder.addSlot(RecipeIngredientRole.INPUT, 63, 26)
                    .addIngredients(recipe.value().cast.get())
                    .setSlotName("cast");
        }

        List<Fluid> fluidFocuses = focuses.getFocuses(NeoForgeTypes.FLUID_STACK).map(focus -> focus.getTypedValue().getIngredient()).map(FluidStack::getFluid).toList();

        var fluidStacks = FluidHelper.fluidStacks(recipe.value().fluid).stream().filter(fluidStack -> fluidFocuses.isEmpty() || fluidFocuses.contains(fluidStack.getFluid())).filter(fluidStack -> fluidStack.getFluid().defaultFluidState().isSource()).toList();

        builder.addSlot(RecipeIngredientRole.INPUT, 26, 26)
                .addIngredients(NeoForgeTypes.FLUID_STACK, fluidStacks)
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.addAll(FluidHelper.formatTooltip(fluidStacks.getFirst()));
                })
                .setFluidRenderer(recipe.value().fluid.amount(), false, 16, 16)
                .setSlotName("fluids");

        builder.addSlot(RecipeIngredientRole.INPUT, 68, 16)
                .addIngredients(NeoForgeTypes.FLUID_STACK, fluidStacks)
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.addAll(FluidHelper.formatTooltip(fluidStacks.getFirst()));
                })
                .setFluidRenderer(recipe.value().fluid.amount(), false, 6,hasCast ? 10 : 26)
                .setSlotName("fluids");

        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 26)
                .addItemStack(recipe.value().result.create())
                .setStandardSlotBackground()
                .setSlotName("result");

        // Add buckets as hidden ingredient
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredients(Ingredient.of(fluidStacks.stream().map(f -> f.getFluid().getBucket())));
    }
}

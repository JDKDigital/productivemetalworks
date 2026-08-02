package cy.jdkdigital.productivemetalworks.integration.jei;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.integration.jei.ingredient.EntityRenderer;
import cy.jdkdigital.productivemetalworks.recipe.EntityMeltingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.FluidHelper;
import cy.jdkdigital.productivemetalworks.util.FluidStackTemplate;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Comparator;

public class EntityMeltingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<EntityMeltingRecipe>>
{
    private final IDrawable background;

    public EntityMeltingRecipeCategory(IGuiHelper guiHelper) {
        super(
                JeiPlugin.ENTITY_MELTING,
                Component.translatable("jei." + ProductiveMetalworks.MODID + ".entity_melting"),
                guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get())),
                165, 68
        );
        this.background = guiHelper.drawableBuilder(Identifier.fromNamespaceAndPath(ProductiveMetalworks.MODID, "textures/gui/jei/entity_melting.png"), 0, 0, 165, 68).setTextureSize(165, 68).build();
    }

    @Override
    public void draw(RecipeHolder<EntityMeltingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics, 0, 0);

        EntityRenderer.render(guiGraphics, 26, 26, recipe.value().entity, Minecraft.getInstance());
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<EntityMeltingRecipe> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 8 && mouseX < 56 && mouseY >= 8 && mouseY < 56) {
            var entity = EntityRenderer.get(recipe.value().entity, Minecraft.getInstance());
            if (entity != null) {
                tooltip.add(entity.getDisplayName());
            }
        }
        super.getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<EntityMeltingRecipe> recipe, IFocusGroup focuses) {
        int maxAmount = recipe.value().result.stream().max(Comparator.comparingInt(FluidStackTemplate::getAmount)).get().getAmount();
        int fWidth = 42/recipe.value().result.size();
        for (int i = 0; i < recipe.value().result.size(); i++) {
            FluidStackTemplate fluid = recipe.value().result.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 112 + (i*fWidth), 8)
                    .addFluidStack(fluid.getFluid(), fluid.getAmount())
                    .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.addAll(FluidHelper.formatTooltip(fluid.create()));
                    })
                    .setFluidRenderer(maxAmount, false, fWidth,52)
                    .setSlotName("fluid" + i);
        }
    }
}

package cy.jdkdigital.productivemetalworks.integration.jei;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.recipe.ItemMeltingRecipe;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemMeltingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<ItemMeltingRecipe>>
{
    private static List<FluidStack> fuels = new ArrayList<>();
    private final IDrawable background;

    public ItemMeltingRecipeCategory(IGuiHelper guiHelper) {
        super(
                JeiPlugin.ITEM_MELTING,
                Component.translatable("jei." + ProductiveMetalworks.MODID + ".item_melting"),
                guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get())),
                165, 68
        );
        this.background = guiHelper.drawableBuilder(ResourceLocation.fromNamespaceAndPath(ProductiveMetalworks.MODID, "textures/gui/jei/item_melting.png"), 0, 0, 165, 68).setTextureSize(165, 68).build();
        if (fuels.isEmpty()) {
            fuels = BuiltInRegistries.FLUID.holders().filter(fluidReference -> fluidReference.getData(MetalworksRegistrator.FUEL_MAP) != null).map(fluidReference -> new FluidStack(fluidReference.value(), 1000)).toList();
        }
    }

    @Override
    public void draw(RecipeHolder<ItemMeltingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics, 0, 0);

        guiGraphics.drawString(Minecraft.getInstance().font, Language.getInstance().getVisualOrder(Component.translatable("jei.productivemetalworks.temperature", recipe.value().minTemperature)), 35, 52, 0xFF000000, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ItemMeltingRecipe> recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 38, 26)
                .addIngredients(recipe.value().item)
                .setStandardSlotBackground()
                .setSlotName("ingredients");

        builder.addSlot(RecipeIngredientRole.INPUT, 11, 8)
                .addIngredients(NeoForgeTypes.FLUID_STACK, fuels.stream().filter(fluidStack -> {
                    var fueldData = fluidStack.getFluidHolder().getData(MetalworksRegistrator.FUEL_MAP);
                    return fueldData != null && fueldData.temperature() >= recipe.value().minTemperature;
                }).toList())
                .setFluidRenderer(1000, false, 16,52)
                .setSlotName("fuel");

        int maxAmount = recipe.value().result.stream().max(Comparator.comparingInt(FluidStack::getAmount)).get().getAmount();
        int fWidth = 42/recipe.value().result.size();
        for (int i = 0; i < recipe.value().result.size(); i++) {
            FluidStack fluid = recipe.value().result.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 112 + (i*fWidth), 8)
                    .addFluidStack(fluid.getFluid(), fluid.getAmount())
                    .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.addAll(FluidHelper.formatTooltip(fluid));
                    })
                    .setFluidRenderer(maxAmount, false, fWidth,52)
                    .setSlotName("fluid" + i);
        }
    }
}

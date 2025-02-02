package cy.jdkdigital.productivemetalworks.integration.jei;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.recipe.SilentGearCastingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.FluidHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

public class SilentGearCastingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<SilentGearCastingRecipe>>
{
    public SilentGearCastingRecipeCategory(IGuiHelper guiHelper) {
        super(
                JeiPlugin.SG_CASTING,
                Component.translatable("jei." + ProductiveMetalworks.MODID + ".sg_casting"),
                guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MetalworksRegistrator.CASTING_TABLE.get())),
                200, 100
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SilentGearCastingRecipe> recipe, IFocusGroup focuses) {
        var level = Minecraft.getInstance().level;

        if (!recipe.value().cast.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 30, 24)
                    .addIngredients(recipe.value().cast)
                    .setStandardSlotBackground()
                    .setSlotName("cast");
        }

        // Rewrite materials to list of fluidstacks
        var fluids = FluidHelper.materialsToFluids(level, recipe.value().material.getItems(), recipe.value().materialCount);

//        var fluidFocus = focuses.getFocuses(NeoForgeTypes.FLUID_STACK, RecipeIngredientRole.INPUT);
//        fluidFocus.forEach(fluidStackIFocus -> {
//            fluidStackIFocus.getTypedValue().getIngredient();
//        });

        builder.addSlot(RecipeIngredientRole.INPUT, 88, 15)
                .addIngredients(NeoForgeTypes.FLUID_STACK, fluids.entrySet().stream().map(entry -> new FluidStack(entry.getKey(), entry.getValue().getSecond())).toList())
                .setStandardSlotBackground()
                .setFluidRenderer(recipe.value().fluid.amount(), false, 16,16)
                .setSlotName("fluids");

        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 24)
                .addItemStack(recipe.value().result)
                .setStandardSlotBackground()
                .setSlotName("result");
    }
}

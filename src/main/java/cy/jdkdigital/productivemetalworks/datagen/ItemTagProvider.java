package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ItemTagProvider extends ItemTagsProvider
{
    public ItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> future, CompletableFuture<TagLookup<Block>> provider, ExistingFileHelper helper) {
        super(output, future, provider, ProductiveMetalworks.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(ModTags.Blocks.FOUNDRY_CONTROLLERS, ModTags.Items.FOUNDRY_CONTROLLERS);
        copy(ModTags.Blocks.FOUNDRY_DRAINS, ModTags.Items.FOUNDRY_DRAINS);
        copy(ModTags.Blocks.FOUNDRY_TANKS, ModTags.Items.FOUNDRY_TANKS);
        copy(ModTags.Blocks.FOUNDRY_WINDOWS, ModTags.Items.FOUNDRY_WINDOWS);
        copy(ModTags.Blocks.FIRE_BRICKS, ModTags.Items.FIRE_BRICKS);

        tag(ModTags.Items.CASTS).add(
                MetalworksRegistrator.CAST_INGOT.get(),
                MetalworksRegistrator.CAST_NUGGET.get(),
                MetalworksRegistrator.CAST_GEM.get(),
                MetalworksRegistrator.CAST_GEAR.get(),
                MetalworksRegistrator.CAST_ROD.get(),
                MetalworksRegistrator.CAST_PLATE.get()
        );

        for (DyeColor color : DyeColor.values()) {
            tag(color.getDyedTag()).add(MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(color).get().asItem());
        }

        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots")))
                .addTag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/meat")))
                .addTag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/shiny_meat")));
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/meat"))).add(MetalworksRegistrator.MEAT_INGOT.get());
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/shiny_meat"))).add(MetalworksRegistrator.SHINY_MEAT_INGOT.get());
    }

    @Override
    public String getName() {
        return "Productive Foundry Item Tags Provider";
    }
}

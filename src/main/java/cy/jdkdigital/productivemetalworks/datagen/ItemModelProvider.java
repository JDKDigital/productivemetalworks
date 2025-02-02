package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public class ItemModelProvider extends net.neoforged.neoforge.client.model.generators.ItemModelProvider
{
    public ItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ProductiveMetalworks.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ProductiveMetalworks.ITEMS.getEntries().forEach(itemHolder -> {
            if (itemHolder.getId().getPath().contains("_bucket")) {
                withExistingParent(itemHolder.getId().getPath(), ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "item/bucket_drip"))
                        .customLoader(DynamicFluidContainerModelBuilder::begin)
                        .fluid(BuiltInRegistries.FLUID.get(itemHolder.getId().withPath(p -> p.replace("_bucket", ""))));
            }
        });

        basicItem(MetalworksRegistrator.CAST_INGOT.get());
        basicItem(MetalworksRegistrator.CAST_NUGGET.get());
        basicItem(MetalworksRegistrator.CAST_GEM.get());
        basicItem(MetalworksRegistrator.CAST_GEAR.get());
        basicItem(MetalworksRegistrator.CAST_ROD.get());
        basicItem(MetalworksRegistrator.CAST_PLATE.get());
        basicItem(MetalworksRegistrator.FIRE_BRICK.get());
        basicItem(MetalworksRegistrator.MEAT_INGOT.get());
        basicItem(MetalworksRegistrator.SHINY_MEAT_INGOT.get());
    }
}

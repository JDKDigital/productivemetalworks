package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class LanguageProvider extends net.neoforged.neoforge.common.data.LanguageProvider
{
    public LanguageProvider(PackOutput output, String locale) {
        super(output, ProductiveMetalworks.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + ProductiveMetalworks.MODID, "Productive Metalworks");
        add(ProductiveMetalworks.MODID + ".message.foundry_formed", "Foundry structure assembled");
        add(ProductiveMetalworks.MODID + ".message.foundry_invalid", "Foundry structure invalid. %s");

        add("jei." + ProductiveMetalworks.MODID + ".item_melting", "Item Melting");
        add("jei." + ProductiveMetalworks.MODID + ".item_casting", "Item Casting");
        add("jei." + ProductiveMetalworks.MODID + ".sg_casting", "Silent Gear Casting");
        add("jei." + ProductiveMetalworks.MODID + ".block_casting", "Block Casting");
        add("jei." + ProductiveMetalworks.MODID + ".fluid_alloying", "Fluid Alloying");
        add(ProductiveMetalworks.MODID + ".tooltip.amount.mb", "%smb");
        add("block." + ProductiveMetalworks.MODID + "foundry_tank.fluid_tooltip", "Contains %smb %s");

        ProductiveMetalworks.ITEMS.getEntries().forEach(registryObject -> {
            if (registryObject.get() instanceof BucketItem) {
                add(registryObject.get(), "Bucket of " + capName(BuiltInRegistries.ITEM.getKey(registryObject.get()).getPath().replace("_bucket", "")));
            } else if (!(registryObject.get() instanceof BlockItem)) {
                add(registryObject.get(), capName(BuiltInRegistries.ITEM.getKey(registryObject.get()).getPath()));
            }
        });
        ProductiveMetalworks.BLOCKS.getEntries().forEach(registryObject -> {
            add(registryObject.get(), capName(BuiltInRegistries.BLOCK.getKey(registryObject.get()).getPath()));
        });
        ProductiveMetalworks.FLUID_TYPES.getEntries().forEach(registryObject -> {
            add(registryObject.get().getDescriptionId(), capName(NeoForgeRegistries.FLUID_TYPES.getKey(registryObject.get()).getPath()));
        });
    }

    @Override
    public String getName() {
        return "Productive Metalworks translation provider";
    }

    private String capName(String name) {
        String[] nameParts = name.split("_");

        for (int i = 0; i < nameParts.length; i++) {
            nameParts[i] = nameParts[i].substring(0, 1).toUpperCase() + nameParts[i].substring(1);
        }

        return String.join(" ", nameParts);
    }
}

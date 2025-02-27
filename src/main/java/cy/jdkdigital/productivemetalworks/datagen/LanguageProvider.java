package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
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

        // Units
        add(ProductiveMetalworks.MODID + ".unit.leftover", "%s mb");
        add(ProductiveMetalworks.MODID + ".unit.hunk.single", "%s Hunk");
        add(ProductiveMetalworks.MODID + ".unit.hunk.multiple", "%s Hunks");
        add(ProductiveMetalworks.MODID + ".unit.chunk.single", "%s Chunk");
        add(ProductiveMetalworks.MODID + ".unit.chunk.multiple", "%s Chunks");
        add(ProductiveMetalworks.MODID + ".unit.nugget.single", "%s Nugget");
        add(ProductiveMetalworks.MODID + ".unit.nugget.multiple", "%s Nuggets");
        add(ProductiveMetalworks.MODID + ".unit.ingot.single", "%s Ingot");
        add(ProductiveMetalworks.MODID + ".unit.ingot.multiple", "%s Ingots");
        add(ProductiveMetalworks.MODID + ".unit.block.single", "%s Block");
        add(ProductiveMetalworks.MODID + ".unit.block.multiple", "%s Blocks");
        add(ProductiveMetalworks.MODID + ".unit.gem.single", "%s Gem");
        add(ProductiveMetalworks.MODID + ".unit.gem.multiple", "%s Gems");
        add(ProductiveMetalworks.MODID + ".unit.pile.single", "%s Pile");
        add(ProductiveMetalworks.MODID + ".unit.pile.multiple", "%s Piles");
        add(ProductiveMetalworks.MODID + ".unit.pane.single", "%s Pane");
        add(ProductiveMetalworks.MODID + ".unit.pane.multiple", "%s Panes");
        add(ProductiveMetalworks.MODID + ".unit.pearl.single", "%s Pearl");
        add(ProductiveMetalworks.MODID + ".unit.pearl.multiple", "%s Pearls");
        add(ProductiveMetalworks.MODID + ".unit.ball.single", "%s Ball");
        add(ProductiveMetalworks.MODID + ".unit.ball.multiple", "%s Balls");
        add(ProductiveMetalworks.MODID + ".unit.shell.single", "%s Shell");
        add(ProductiveMetalworks.MODID + ".unit.shell.multiple", "%s Shells");
        add(ProductiveMetalworks.MODID + ".unit.rod.single", "%s Rod");
        add(ProductiveMetalworks.MODID + ".unit.rod.multiple", "%s Rods");
        add(ProductiveMetalworks.MODID + ".unit.scrap.single", "%s Scrap");
        add(ProductiveMetalworks.MODID + ".unit.scrap.multiple", "%s Scraps");

        add("jei." + ProductiveMetalworks.MODID + ".item_melting", "Item Melting");
        add("jei." + ProductiveMetalworks.MODID + ".item_casting", "Item Casting");
        add("jei." + ProductiveMetalworks.MODID + ".sg_casting", "Silent Gear Casting");
        add("jei." + ProductiveMetalworks.MODID + ".block_casting", "Block Casting");
        add("jei." + ProductiveMetalworks.MODID + ".fluid_alloying", "Fluid Alloying");
        add("jade." + ProductiveMetalworks.MODID + ".cooling", "Cooling");
        add("block." + ProductiveMetalworks.MODID + "foundry_tank.fluid_tooltip", "Contains %smb %s");

        add("productivebees.ingredient.description.soul_lava_bee", "To acquire this bee, look up the crafting recipe for its spawn egg.");
        add("productivebees.ingredient.description.allthemodium_bee", "To acquire this bee, look up the crafting recipe for its spawn egg.");
        add("productivebees.ingredient.description.vibranium_bee", "To acquire this bee, look up the crafting recipe for its spawn egg.");
        add("productivebees.ingredient.description.unobtainium_bee", "To acquire this bee, look up the crafting recipe for its spawn egg.");

        add("book.productivemetalworks.name", "Big Book of Metallurgy");
        add("book.productivemetalworks.landing_text", "A shallow look into the world of metalworking.$(br2)Put on your gloves 'cause it's gonna get hot.");

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

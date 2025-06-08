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
        add("jei." + ProductiveMetalworks.MODID + ".entity_melting", "Entity Melting");
        add("jei." + ProductiveMetalworks.MODID + ".item_casting", "Item Casting");
        add("jei." + ProductiveMetalworks.MODID + ".sg_casting", "Silent Gear Casting");
        add("jei." + ProductiveMetalworks.MODID + ".block_casting", "Block Casting");
        add("jei." + ProductiveMetalworks.MODID + ".fluid_alloying", "Fluid Alloying");
        add("jei." + ProductiveMetalworks.MODID + ".temperature", "Temp: %s C");
        add("jade." + ProductiveMetalworks.MODID + ".cooling", "Cooling");
        add("block." + ProductiveMetalworks.MODID + "foundry_tank.fluid_tooltip", "Contains %smb %s");

        add("config.jade.plugin_productivemetalworks.casting", "Productive Metalworks");

        add("productivebees.information.upgrade.upgrade_stability", "Disables fluid alloying in the Foundry.");
        add("productivemetalworks.information.upgrade.upgrade_stability", "Disables fluid alloying in the Foundry.");
        add("productivemetalworks.information.upgrade.upgrade_time", "Can be installed in machines for faster processing.\nMultiple upgrades can be installed for a greater time decrease.");
        add("productivemetalworks.information.upgrade.upgrade_time_2", "It's twice as good as the other one.");

        add("productivebees.ingredient.description.soul_lava_bee", "To acquire this bee, look up the crafting recipe for its spawn egg.");
        add("productivebees.ingredient.description.allthemodium_bee", "To acquire this bee, look up the crafting recipe for its spawn egg.");
        add("productivebees.ingredient.description.vibranium_bee", "To acquire this bee, look up the crafting recipe for its spawn egg.");
        add("productivebees.ingredient.description.unobtainium_bee", "To acquire this bee, look up the crafting recipe for its spawn egg.");

        add("gui." + ProductiveMetalworks.MODID + ".temperature", "Temperature: %s C");

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


        add("book.productivemetalworks.name", "Big Book of Metallurgy");
        add("book.productivemetalworks.landing_text", "A shallow look into the world of metalworking.$(br2)Put on your gloves 'cause it's gonna get hot.");
        add("book.productivemetalworks.intro_category.title", "An Introduction");
        add("book.productivemetalworks.intro_category.text", "Getting started with the basics of building your foundry.");
        add("book.productivemetalworks.intro_page.title", "The Foundry");
        add("book.productivemetalworks.intro_page.metalworks", "Welcome to Productive Metalworks. A mod inspired by the Tinker's Construct Smeltery. You will find some differences, and lots of similarities. Do note, this mod is NOT a tool making mod, it is only the foundry.");
        add("book.productivemetalworks.intro_page.sgearmetalworks", "Welcome to Productive Metalworks. A mod inspired by the Tinker's Construct Smeltery. You will find some differences, and lots of similarities. This mod does not add any tool or weapons but has compatibility with Silent Gear and modifies how you acquire gear parts. More on this in the $(l:sgear)SGear section$(/l).");
        add("book.productivemetalworks.intro_page.obtaining", "In order to construct the Foundry you need fire bricks which are obtained by smelting fire clay. You will start by making the black fire bricks. The black versions of the blocks are the base ones and can be dyed to get the different colored ones.");
        add("book.productivemetalworks.intro_page.building", "The blocks you will need to achieve your foundry are:$(br)" +
                    "* Foundry Controller$(br)" +
                    "* Foundry Tank$(br)" +
                    "* Liquid Heating Coils$(br)" +
                    "* Fire Bricks or Foundry Windows$(br)" +
                    "* Foundry Drain and Taps$(br)" +
                    "* Casting Basin & Table$(br2)" +
                    "The foundry can be as small as  1x1 and any rectangular shape. To the right you can see an example of a square 3x3 setup.");
        add("book.productivemetalworks.intro_page.multiblock", "3x3 Foundry");
        add("book.productivemetalworks.intro_page.structure", "Be aware that the base level of the foundry will need to be made from heating coils. These enable you to actually heat up the foundry. The corners of the foundry multiblock are optional." +
                "$(br)The foundry by default will smelt raw ore at a 2x rate and the melting rate is determined by the fuel used. Check JEI for all the melting recipes");
        add("book.productivemetalworks.intro_page.casting", "To get usable resources from the melted materials you need to cast ingots or blocks from them using the casting basin or table with a cast.$(br2)" +
                "The Foundry can easily be automated by using a redstone pulse or clock on the tap or by replacing the tap with any other fluid transport pipe.");

        add("book.productivemetalworks.sgear_category.title", "Silent Gear Metalworks");
        add("book.productivemetalworks.sgear_category.text", "Silent Gear casting with the Foundry.");
        add("book.productivemetalworks.sgear_page.title", "Gear Casting");
        add("book.productivemetalworks.sgear_page.parts", "Gear parts that use materials which have a molten version will have to be cast in the foundry. You'll need a cast for each gear type, pickaxe, shovel, tip etc. " +
                "To get a cast you have to make a gear part from simple materials like stone, wood, flint or bone, then use that gear part to make the cast.");
        add("book.productivemetalworks.sgear_page.casts", "There's a gear cast for every gear type that requires casting, this includes tips and tool rods. Please note that the tool rod cast is different from the regular rod cast.");
        add("book.productivemetalworks.sgear_page.grading", "There are a few other changes this mod makes to Silent Gear. The first one is the casting as already mentions, the other changes are that in order to grade or star charge your tool you have to do it to the final gear part and NOT the individual materials." +
                "Grading and star charging needs to be done on the gear part multiple times as it will act on each individual material in the part one at a time.");
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

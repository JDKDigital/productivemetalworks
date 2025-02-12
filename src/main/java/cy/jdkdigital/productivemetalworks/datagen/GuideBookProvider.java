package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import xyz.brassgoggledcoders.patchouliprovider.BookBuilder;
import xyz.brassgoggledcoders.patchouliprovider.PatchouliBookProvider;
import xyz.brassgoggledcoders.patchouliprovider.page.MultiblockPageBuilder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GuideBookProvider extends PatchouliBookProvider
{
    public GuideBookProvider(PackOutput packOutput, String locale, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, ProductiveMetalworks.MODID, locale, registries);
    }

    @Override
    protected void addBooks(Consumer<BookBuilder> consumer, HolderLookup.Provider provider) {
        var bookBuilder = createBookBuilder("guide", "Big Book of Metallurgy", "A shallow look into the world of metalworking.$(br2)Put on your gloves 'cause it's gonna get hot.", provider);
        bookBuilder
                .setVersion("1")
                .setNameplateColor("444444")
                .setModel("productivemetalworks:book")
                .setBookTexture("patchouli:textures/gui/book_red.png")
                .setShowProgress(false)
                .setUseResourcePack(true)
                .setCreativeTab(MetalworksRegistrator.TAB_KEY.location().toString());

        var introCategory = bookBuilder.addCategory("intro", "An Introduction", "Getting started with the basics of building your foundry.", MetalworksRegistrator.FIRE_BRICK.get().asItem().getDefaultInstance());
        var introEntry = introCategory.addEntry("intro", "The Foundry", MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get().asItem().getDefaultInstance());
            introEntry.addTextPage("Welcome to Productive Metalworks. A mod inspired by the Tinker's Construct Smeltery. You will find some differences, and lots of similarities. Do note, this mod is NOT a tool making mod, it is only the foundry.");
            introEntry.addTextPage("In order to construct the Foundry you need fire bricks which are obtained by smelting fire clay. You will start by making the black fire bricks. These are the base ones and can be dyed to get the different colored ones.");
            introEntry.addTextPage("The blocks you will need to achieve your foundry are:$(br)" +
                    "* Foundry Controller$(br)" +
                    "* Foundry Tank$(br) " +
                    "* Liquid Heating Coils$(br) " +
                    "* Foundry Drain$(br) " +
                    "* Fire Bricks or Foundry Windows$(br) " +
                    "* Casting Basin & Table");
            introEntry.addMultiblockPage("foundry", new MultiblockPageBuilder.MultiblockBuilder()
                    .setPattern(new String[][] {
                        {" WWW ", "W   W", "W   W", "W   W", " WWW ", "     "},
                        {" WWW ", "W   W", "W 0 W", "W   W", " TCDP", "   Q "},
                        {"     ", " BBB ", " BBB ", " BBB ", "    A", "   S "}
                    })
                    .setMapping(Map.of(
                        "C", "productivemetalworks:black_foundry_controller[facing=east,attached=true]",
                        "T", "productivemetalworks:black_foundry_tank[facing=east]",
                        "D", "productivemetalworks:black_foundry_drain[facing=east]",
                        "W", "productivemetalworks:black_fire_bricks",
                        "B", "productivemetalworks:liquid_heating_coil[attached=true]",
                        "A", "productivemetalworks:casting_table",
                        "S", "productivemetalworks:casting_basin",
                        "P", "productivemetalworks:foundry_tap[facing=south]",
                        "Q", "productivemetalworks:foundry_tap[facing=east]"
                    ))
                    .build()).setName("3x3 Foundry").build();
            introEntry.addTextPage("A major difference is you will need to make the bottom of your foundry liquid heating coils. These enable you to actually heat up the foundry. The corners of the foundry multiblock are optional.");
            introEntry.addTextPage("$(br)The foundry by default will smelt raw ore at a 1.3x rate and the melting rate is determined by the fuel used.");
            introEntry.build();

        bookBuilder.build(consumer);
    }
}

package cy.jdkdigital.productivemetalworks.datagen;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
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
        var bookBuilder = createBookBuilder("guide", "book.productivemetalworks.name", "book.productivemetalworks.landing_text", provider);
        bookBuilder
                .setVersion("1")
                .setNameplateColor("444444")
                .setModel("productivemetalworks:book")
                .setBookTexture("patchouli:textures/gui/book_red.png")
                .setShowProgress(false)
                .setUseResourcePack(true)
                .setI18n(true)
                .setCreativeTab(MetalworksRegistrator.TAB_KEY.location().toString());

        var introCategory = bookBuilder.addCategory("intro", "book.productivemetalworks.intro_category.title", "book.productivemetalworks.intro_category.text", MetalworksRegistrator.FIRE_BRICK.get().asItem().getDefaultInstance());
        var introEntry = introCategory.addEntry("intro", "book.productivemetalworks.intro_page.title", MetalworksRegistrator.FOUNDRY_CONTROLLERS.get(DyeColor.BLACK).get().asItem().getDefaultInstance());
            introEntry.addTextPage("book.productivemetalworks.intro_page.metalworks").setFlag("!mod:sgearmetalworks");
            introEntry.addTextPage("book.productivemetalworks.intro_page.sgearmetalworks").setFlag("mod:sgearmetalworks");
            introEntry.addTextPage("book.productivemetalworks.intro_page.obtaining");
            introEntry.addTextPage("book.productivemetalworks.intro_page.building");
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
                    .build()).setName("book.productivemetalworks.intro_page.multiblock").build();
        introEntry.addTextPage("book.productivemetalworks.intro_page.structure");
        introEntry.addTextPage("book.productivemetalworks.intro_page.casting");
            introEntry.build();

        var sgearCategory = bookBuilder.addCategory("sgear", "book.productivemetalworks.sgear_category.title", "book.productivemetalworks.sgear_category.text", Items.DIAMOND_SWORD.getDefaultInstance()).setFlag("mod:sgearmetalworks");

        var sgearEntry = sgearCategory.addEntry("sgear", "book.productivemetalworks.sgear_page.title", MetalworksRegistrator.CASTING_TABLE.get().asItem().getDefaultInstance()).setFlag("mod:sgearmetalworks");
        sgearEntry.addTextPage("book.productivemetalworks.sgear_page.parts").setFlag("mod:sgearmetalworks");
        sgearEntry.addTextPage("book.productivemetalworks.sgear_page.casts").setFlag("mod:sgearmetalworks");
        sgearEntry.addTextPage("book.productivemetalworks.sgear_page.grading").setFlag("mod:sgearmetalworks");

        bookBuilder.build(consumer);
    }
}

package cy.jdkdigital.productivemetalworks;

import com.mojang.logging.LogUtils;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

// PORT-TODO (26.1): ponder/create integration excluded until Create ships a 26.1.2 build.
//import cy.jdkdigital.productivemetalworks.integration.ponder.MetalworksPonderPlugin;
//import net.createmod.ponder.foundation.PonderIndex;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ProductiveMetalworks.MODID)
public class ProductiveMetalworks
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "productivemetalworks";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ProductiveMetalworks(IEventBus modEventBus, ModContainer modContainer)
    {
        // GameTest hook. Touching MetalworksGameTests.MAX_TICKS forces its <clinit>, which calls
        // TestFunctions.register(...) for every test; init() then unfreezes BuiltInRegistries.TEST_FUNCTION,
        // registers each entry, and refreezes. No-op cost in production (the gametest path is only reached
        // when GameTestHooks.isGametestEnabled() is true).
        @SuppressWarnings("unused")
        Object forceTestsLoad = cy.jdkdigital.productivemetalworks.gametest.MetalworksGameTests.MAX_TICKS;
        cy.jdkdigital.productivemetalworks.gametest.TestFunctions.init();

        MetalworksRegistrator.register();

        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        FLUIDS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        CONTAINER_TYPES.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForgeMod.enableMilkFluid();

        if (ModList.get().isLoaded("invtweaks")) {
            InterModComms.sendTo("invtweaks", "blacklist-screen", () -> "cy.jdkdigital.productivemetalworks.client.screen.*");
        }

        // PORT-TODO (26.1): restore once Create/ponder publishes a 26.1.2 build and integration/ponder is re-included.
//        if(FMLEnvironment.dist.isClient()) {
//            PonderIndex.addPlugin(new MetalworksPonderPlugin());
//        }
    }
}

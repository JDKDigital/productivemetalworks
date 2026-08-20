package cy.jdkdigital.productivemetalworks.event;

import cy.jdkdigital.productivelib.event.UpgradeTooltipEvent;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.integration.jei.ingredient.EntityRenderer;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.RecipeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = ProductiveMetalworks.MODID)
public class EventHandler
{
    @SubscribeEvent
    public static void addUpgradeTooltip(UpgradeTooltipEvent event) {
        var upgradeType = BuiltInRegistries.ITEM.getKey(event.getStack().getItem());

        String tPrefix = "productivemetalworks.information.upgrade." + upgradeType.getPath() + ".";
        switch (upgradeType.getPath()) {
            case "upgrade_time", "upgrade_time_2", "upgrade_stability" -> {
                event.addValidBlock(Component.translatable("productivemetalworks.devices.foundry_controller"), tPrefix + "foundry_controller");
            }
        }
    }

    // 26.1: opt the foundry recipe types into client sync so JEI can display them (see MetalworksRecipeCache).
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        RecipeHelper.clearCaches();
        event.sendRecipes(
                MetalworksRegistrator.ITEM_MELTING_TYPE.get(),
                MetalworksRegistrator.ITEM_CASTING_TYPE.get(),
                MetalworksRegistrator.BLOCK_CASTING_TYPE.get(),
                MetalworksRegistrator.FLUID_ALLOYING_TYPE.get()
        );
    }

    @SubscribeEvent
    public static void levelUnload(final LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            EntityRenderer.cache.clear();
        }
        RecipeHelper.clearCaches();
    }
}

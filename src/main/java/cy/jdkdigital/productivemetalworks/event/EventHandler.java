package cy.jdkdigital.productivemetalworks.event;

import cy.jdkdigital.productivelib.event.UpgradeTooltipEvent;
import cy.jdkdigital.productivelib.registry.LibItems;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import cy.jdkdigital.productivemetalworks.integration.jei.ingredient.EntityRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
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

    @SubscribeEvent
    private static void levelUnload(final LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            EntityRenderer.cache.clear();
        }
    }
}

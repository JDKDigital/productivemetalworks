package cy.jdkdigital.productivemetalworks.event;

import cy.jdkdigital.productivelib.event.UpgradeTooltipEvent;
import cy.jdkdigital.productivelib.registry.LibItems;
import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ProductiveMetalworks.MODID)
public class EventHandler
{
    @SubscribeEvent
    public static void addUpgradeTooltip(UpgradeTooltipEvent event) {
        var upgradeType = BuiltInRegistries.ITEM.getKey(event.getStack().getItem());

        if (!ModList.get().isLoaded("productivebees") &&
                (
                    event.getStack().is(LibItems.UPGRADE_TIME.get()) ||
                    event.getStack().is(LibItems.UPGRADE_TIME_2.get()) ||
                    event.getStack().is(LibItems.UPGRADE_STABILITY.get())
                )
        ) {
            event.getTooltipComponents().add(Component.translatable("productivemetalworks.information.upgrade." + upgradeType).withStyle(ChatFormatting.GOLD));
        }

        switch (upgradeType.getPath()) {
            case "upgrade_time", "upgrade_time_2", "upgrade_stability" -> {
                event.addValidBlock(Component.literal("Foundry Controller"));
            }
        }
    }
}

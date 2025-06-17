package cy.jdkdigital.productivemetalworks.integration.ponder;

import net.minecraft.resources.ResourceLocation;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;

import org.jetbrains.annotations.NotNull;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;

public class MetalworksPonderPlugin implements PonderPlugin {

    @Override
    public @NotNull String getModId() {
        return ProductiveMetalworks.MODID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        MetalworksPonderScenes.register(helper);
    }

    @Override
    public void registerTags(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
        MetalworksPonderTags.register(helper);
    }
}

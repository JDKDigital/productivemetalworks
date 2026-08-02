package cy.jdkdigital.productivemetalworks.integration.ponder;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class MetalworksPonderPlugin implements PonderPlugin {

    @Override
    public @NotNull String getModId() {
        return ProductiveMetalworks.MODID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<Identifier> helper) {
        MetalworksPonderScenes.register(helper);
    }

    @Override
    public void registerTags(@NotNull PonderTagRegistrationHelper<Identifier> helper) {
        MetalworksPonderTags.register(helper);
    }
}

package cy.jdkdigital.productivemetalworks.integration.jade;

import cy.jdkdigital.productivemetalworks.ProductiveMetalworks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

// 26.1 (MC 1.21.6+): client-only component provider. Decodes the data streamed by CastingTableServerProvider.
public class CastingTableClientProvider implements IBlockComponentProvider
{
    public static final CastingTableClientProvider INSTANCE = new CastingTableClientProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CastingTableServerProvider.Data data = CastingTableServerProvider.INSTANCE.decodeFromData(accessor).orElse(null);
        if (data == null) {
            return;
        }
        if (data.isCooling()) {
            tooltip.add(Component.translatable("jade." + ProductiveMetalworks.MODID + ".cooling"));
        }
    }

    @Override
    public Identifier getUid() {
        return CastingTableServerProvider.UID;
    }
}

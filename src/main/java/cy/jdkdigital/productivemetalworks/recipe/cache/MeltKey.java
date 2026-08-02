package cy.jdkdigital.productivemetalworks.recipe.cache;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// Cache key for item-melting lookups. Built from the immutable component patch (safe as a HashMap key even
// though an ItemStack's live component map is mutable) so lookups don't serialize components to a String.
public record MeltKey(Item item, DataComponentPatch components, int temperature) {
    public static MeltKey of(ItemStack item, int temperature) {
        return new MeltKey(item.getItem(), item.getComponentsPatch(), temperature);
    }
}

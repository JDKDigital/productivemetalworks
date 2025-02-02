package cy.jdkdigital.productivemetalworks.integration.silentgear;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.silentchaos512.gear.api.material.Material;
import net.silentchaos512.gear.gear.material.MaterialInstance;
import net.silentchaos512.gear.item.CompoundPartItem;

public class SilentCompatHelper
{
    public static boolean matches(ItemStack result, RecipeInput inv, Level level) {
        if (!(result.getItem() instanceof CompoundPartItem outputItem)) {
            throw new IllegalArgumentException("result is not a compound part item: " + result);
        }

        Material first = null;

        if (inv instanceof CraftingInput) {
            for (int i = 0; i < inv.size(); ++i) {
                ItemStack stack = inv.getItem(i);
                MaterialInstance mat = MaterialInstance.from(stack);

                if (mat != null) {
                    if (!mat.get().isCraftingAllowed(mat, outputItem.getPartType(), outputItem.getGearType(), (CraftingInput) inv)) {
                        return false;
                    }

                    // If classic mixing is disabled, all materials must be the same
                    if (first == null) {
                        first = mat.get();
                    } else if (first != mat.get()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}

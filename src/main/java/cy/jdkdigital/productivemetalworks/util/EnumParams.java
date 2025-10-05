package cy.jdkdigital.productivemetalworks.util;

import cy.jdkdigital.productivemetalworks.common.damagesource.FoundryDeathMessageProvider;
import net.neoforged.neoforge.common.damagesource.IDeathMessageProvider;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class EnumParams
{
    public static Object getDamageTypesParameter(int idx, Class<?> type) {
        return type.cast(switch (idx) {
            case 0 -> "productivemetalworks:foundry";
            case 1 -> FoundryDeathMessageProvider.INSTANCE;
            default -> throw new IllegalArgumentException("Unexpected parameter index: " + idx);
        });
    }
}

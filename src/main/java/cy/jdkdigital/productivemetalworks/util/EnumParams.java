package cy.jdkdigital.productivemetalworks.util;

import cy.jdkdigital.productivemetalworks.common.damagesource.FoundryDeathMessageProvider;

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

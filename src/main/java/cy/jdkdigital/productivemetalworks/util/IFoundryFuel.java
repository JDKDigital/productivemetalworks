package cy.jdkdigital.productivemetalworks.util;

import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;
import org.jetbrains.annotations.Nullable;

public interface IFoundryFuel
{
    @Nullable
    FuelMap getFuelData();

    int getAmount();

    boolean isEmpty();

    void grow(int amount);

    void shrink(int amount);
}
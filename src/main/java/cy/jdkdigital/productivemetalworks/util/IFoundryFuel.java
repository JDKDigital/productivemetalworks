package cy.jdkdigital.productivemetalworks.util;

import cy.jdkdigital.productivemetalworks.common.datamap.FuelMap;

public interface IFoundryFuel
{
    FuelMap getFuelData();

    int getAmount();

    boolean isEmpty();

    void grow(int amount);

    void shrink(int amount);
}
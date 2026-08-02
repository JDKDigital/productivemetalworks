package cy.jdkdigital.productivemetalworks.util;

import cy.jdkdigital.productivelib.util.MultiFluidTank;

/**
 * Thin alias kept so existing field declarations and anonymous-subclass overrides compile
 * unchanged. The bridge methods this class used to define ({@code fill}/{@code drain}/
 * {@code getFluid}/{@code setFluid}/{@code onContentsChanged}/etc.) all live in
 * {@link MultiFluidTank} now.
 */
public class ModFluidTank extends MultiFluidTank
{
    public ModFluidTank(int capacity) {
        super(1, capacity);
    }

    public ModFluidTank(int tanks, int capacity) {
        super(tanks, capacity);
    }
}

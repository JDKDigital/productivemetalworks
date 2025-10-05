package cy.jdkdigital.productivemetalworks.common.damagesource;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.IDeathMessageProvider;
import org.jetbrains.annotations.Nullable;

public class FoundryDeathMessageProvider implements IDeathMessageProvider
{
    public static IDeathMessageProvider INSTANCE = new FoundryDeathMessageProvider();

    @Override
    public Component getDeathMessage(LivingEntity livingEntity, CombatEntry combatEntry, @Nullable CombatEntry combatEntry1) {
        DamageSource dmgSrc = combatEntry.source();
        if (dmgSrc.getEntity() == null && dmgSrc.getDirectEntity() == null) {
            LivingEntity killer = livingEntity.getKillCredit();
            return killer != null ?
                    Component.translatable("death.attack.productivemetalworks.foundry_damage.player." + livingEntity.getRandom().nextInt(1,9), livingEntity.getDisplayName(), killer.getDisplayName()) :
                    Component.translatable("death.attack.productivemetalworks.foundry_damage." + livingEntity.getRandom().nextInt(1,9), livingEntity.getDisplayName());
        } else {
            Component component = dmgSrc.getEntity() == null ? dmgSrc.getDirectEntity().getDisplayName() : dmgSrc.getEntity().getDisplayName();

            return Component.translatable("death.attack.productivemetalworks.foundry_damage", livingEntity.getDisplayName(), component);
        }
    }
}

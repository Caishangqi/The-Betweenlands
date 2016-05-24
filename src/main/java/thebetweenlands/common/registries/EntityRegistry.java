package thebetweenlands.common.registries;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import thebetweenlands.common.TheBetweenlands;
import thebetweenlands.common.entity.mobs.*;
import thebetweenlands.common.entity.projectiles.EntitySnailPoisonJet;

public class EntityRegistry {
    private static int id;

    private static void registerEntity(Class<? extends Entity> entityClass, String name, int trackingRange, int trackingFrequency, boolean velocityUpdates) {
        net.minecraftforge.fml.common.registry.EntityRegistry.registerModEntity(entityClass, name, id, TheBetweenlands.instance, trackingRange, trackingFrequency, velocityUpdates);
        id++;
    }

    private static void registerEntity(Class<? extends Entity> entityClass, String name) {
        net.minecraftforge.fml.common.registry.EntityRegistry.registerModEntity(entityClass, name, id, TheBetweenlands.instance, 64, 3, true);
        id++;
    }

    private static void registerEntity(Class<? extends EntityLiving> entityClass, String name, int eggBackgroundColor, int eggForegroundColor, int trackingRange, int trackingFrequency, boolean velocityUpdates) {
        registerEntity(entityClass, name, trackingRange, trackingFrequency, velocityUpdates);
        net.minecraftforge.fml.common.registry.EntityRegistry.registerEgg(entityClass, eggBackgroundColor, eggForegroundColor);
    }

    private static void registerEntity(Class<? extends EntityLiving> entityClass, String name, int eggBackgroundColor, int eggForegroundColor) {
        registerEntity(entityClass, name);
        net.minecraftforge.fml.common.registry.EntityRegistry.registerEgg(entityClass, eggBackgroundColor, eggForegroundColor);
    }

    public void preInit() {
        id = 0;
        registerEntity(EntityAngler.class, "angler", 0x243B0B, 0x00FFFF);
        registerEntity(EntitySwampHag.class, "swampHag", 0x0B3B0B, 0xDBA901);
        registerEntity(EntityDragonfly.class, "dragonfly", 0x31B53C, 0x779E3C);
        registerEntity(EntityBloodSnail.class, "bloodSnail", 0x8E9456, 0xB3261E);
        registerEntity(EntityMireSnail.class, "mireSnail", 0x8E9456, 0xF2FA96);
        registerEntity(EntityMireSnailEgg.class, "mireSnailEgg");
        registerEntity(EntitySnailPoisonJet.class, "snailPoisonJet");
        registerEntity(EntityLurker.class, "lurker", 0x283320, 0x827856);
        registerEntity(EntityBlindCaveFish.class, "blindCaveFish", 0xD0D1C2, 0xECEDDF);
        registerEntity(EntityChiromaw.class, "chiromaw", 0x3F5A69, 0xA16A77);
    }
}
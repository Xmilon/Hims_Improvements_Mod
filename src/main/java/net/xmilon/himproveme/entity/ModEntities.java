package net.xmilon.himproveme.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.entity.custom.DodoEntity;

public class ModEntities {
    public static final EntityType<DodoEntity> DODO = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(HimProveMe.MOD_ID, "dodo"),
            EntityType.Builder.create(DodoEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.8f, 1.5f).build());


    public static void registerModEntities(){
        HimProveMe.LOGGER.info("Registering Mod Entities for " + HimProveMe.MOD_ID);
        SpawnRestriction.register(
                DODO,
                SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                DodoEntity::canSpawn
        );
    }
}

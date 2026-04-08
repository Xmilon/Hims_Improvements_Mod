package net.xmilon.himproveme.world.ancientcity;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

import java.util.Optional;

/**
 * Thin wrapper around the structure accessor so Ancient City lookups stay consistent across the mod.
 */
public final class AncientCityLocator {
    private AncientCityLocator() {
    }

    /**
     * Resolves the Ancient City containing the given position, if one is currently cached by the world.
     */
    public static Optional<CityReference> locate(ServerWorld world, BlockPos pos) {
        Registry<Structure> registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        Optional<RegistryEntry.Reference<Structure>> ancientCityEntry = registry.getEntry(StructureKeys.ANCIENT_CITY);
        if (ancientCityEntry.isEmpty()) {
            return Optional.empty();
        }

        StructureStart start = world.getStructureAccessor().getStructureContaining(pos, ancientCityEntry.get().value());
        if (start == StructureStart.DEFAULT || !start.hasChildren()) {
            return Optional.empty();
        }

        BlockBox box = start.getBoundingBox();
        if (!box.contains(pos)) {
            return Optional.empty();
        }

        return Optional.of(new CityReference(AncientCityKey.from(world, box), box));
    }

    /**
     * Lightweight result object that exposes both the storage key and the live bounding box.
     */
    public record CityReference(AncientCityKey key, BlockBox boundingBox) {
    }
}

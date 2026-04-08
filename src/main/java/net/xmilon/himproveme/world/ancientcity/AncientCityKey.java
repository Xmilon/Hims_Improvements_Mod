package net.xmilon.himproveme.world.ancientcity;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

/**
 * Stable identifier for one Ancient City instance based on its bounding box and dimension.
 */
public record AncientCityKey(
        Identifier dimensionId,
        int minX,
        int minY,
        int minZ,
        int maxX,
        int maxY,
        int maxZ
) {
    /**
     * Builds a stable key from the structure bounding box we get back from the structure accessor.
     */
    public static AncientCityKey from(ServerWorld world, BlockBox box) {
        return new AncientCityKey(
                world.getRegistryKey().getValue(),
                box.getMinX(),
                box.getMinY(),
                box.getMinZ(),
                box.getMaxX(),
                box.getMaxY(),
                box.getMaxZ()
        );
    }

    /**
     * Recreates the city bounding box so runtime systems can use the key without holding onto structure objects.
     */
    public BlockBox toBlockBox() {
        return new BlockBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Returns true when the given position is still inside the stored Ancient City bounds.
     */
    public boolean contains(BlockPos pos) {
        return this.toBlockBox().contains(pos);
    }

    /**
     * Serializes the key to a compact string for persistent world state.
     */
    public String asStorageKey() {
        return this.dimensionId + "|" + this.minX + "," + this.minY + "," + this.minZ + "|" + this.maxX + "," + this.maxY + "," + this.maxZ;
    }

    /**
     * Parses a city key that was stored in persistent data.
     */
    public static AncientCityKey fromStorageKey(String value) {
        String[] parts = value.split("\\|");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Ancient City key: " + value);
        }

        String[] minParts = parts[1].split(",");
        String[] maxParts = parts[2].split(",");
        if (minParts.length != 3 || maxParts.length != 3) {
            throw new IllegalArgumentException("Invalid Ancient City key: " + value);
        }

        return new AncientCityKey(
                Identifier.of(parts[0]),
                Integer.parseInt(minParts[0]),
                Integer.parseInt(minParts[1]),
                Integer.parseInt(minParts[2]),
                Integer.parseInt(maxParts[0]),
                Integer.parseInt(maxParts[1]),
                Integer.parseInt(maxParts[2])
        );
    }
}

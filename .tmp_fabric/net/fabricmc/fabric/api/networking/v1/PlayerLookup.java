/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.networking.v1;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.class_1297;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2802;
import net.minecraft.class_3215;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3898;
import net.minecraft.class_5629;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.fabric.mixin.networking.accessor.ChunkMapAccessor;
import net.fabricmc.fabric.mixin.networking.accessor.EntityTrackerAccessor;

/**
 * Helper methods to lookup players in a server.
 *
 * <p>The word "tracking" means that an entity/chunk on the server is known to a player's client (within in view distance) and the (block) entity should notify tracking clients of changes.
 *
 * <p>These methods should only be called on the server thread and only be used on logical a server.
 */
public final class PlayerLookup {
	/**
	 * Gets all the players on the minecraft server.
	 *
	 * <p>The returned collection is immutable.
	 *
	 * @param server the server
	 * @return all players on the server
	 */
	public static Collection<class_3222> all(MinecraftServer server) {
		Objects.requireNonNull(server, "The server cannot be null");

		// return an immutable collection to guard against accidental removals.
		if (server.method_3760() != null) {
			return Collections.unmodifiableCollection(server.method_3760().method_14571());
		}

		return Collections.emptyList();
	}

	/**
	 * Gets all the players in a server world.
	 *
	 * <p>The returned collection is immutable.
	 *
	 * @param world the server world
	 * @return the players in the server world
	 */
	public static Collection<class_3222> world(class_3218 world) {
		Objects.requireNonNull(world, "The world cannot be null");

		// return an immutable collection to guard against accidental removals.
		return Collections.unmodifiableCollection(world.method_18456());
	}

	/**
	 * Gets all players tracking a chunk in a server world.
	 *
	 * @param world the server world
	 * @param pos   the chunk in question
	 * @return the players tracking the chunk
	 */
	public static Collection<class_3222> tracking(class_3218 world, class_1923 pos) {
		Objects.requireNonNull(world, "The world cannot be null");
		Objects.requireNonNull(pos, "The chunk pos cannot be null");

		return world.method_14178().field_17254.method_17210(pos, false);
	}

	/**
	 * Gets all players tracking an entity in a server world.
	 *
	 * <p>The returned collection is immutable.
	 *
	 * <p><b>Warning</b>: If the provided entity is a player, it is not
	 * guaranteed by the contract that said player is included in the
	 * resulting stream.
	 *
	 * @param entity the entity being tracked
	 * @return the players tracking the entity
	 * @throws IllegalArgumentException if the entity is not in a server world
	 */
	public static Collection<class_3222> tracking(class_1297 entity) {
		Objects.requireNonNull(entity, "Entity cannot be null");
		class_2802 manager = entity.method_73183().method_8398();

		if (manager instanceof class_3215) {
			class_3898 chunkLoadingManager = ((class_3215) manager).field_17254;
			EntityTrackerAccessor tracker = ((ChunkMapAccessor) chunkLoadingManager).getEntityTrackers().get(entity.method_5628());

			// return an immutable collection to guard against accidental removals.
			if (tracker != null) {
				return tracker.getPlayersTracking()
						.stream().map(class_5629::method_32311).collect(Collectors.toUnmodifiableSet());
			}

			return Collections.emptySet();
		}

		throw new IllegalArgumentException("Only supported on server worlds!");
	}

	/**
	 * Gets all players tracking a block entity in a server world.
	 *
	 * @param blockEntity the block entity
	 * @return the players tracking the block position
	 * @throws IllegalArgumentException if the block entity is not in a server world
	 */
	public static Collection<class_3222> tracking(class_2586 blockEntity) {
		Objects.requireNonNull(blockEntity, "BlockEntity cannot be null");

		//noinspection ConstantConditions - IJ intrinsics don't know hasWorld == true will result in no null
		if (!blockEntity.method_11002() || blockEntity.method_10997().method_8608()) {
			throw new IllegalArgumentException("Only supported on server worlds!");
		}

		return tracking((class_3218) blockEntity.method_10997(), blockEntity.method_11016());
	}

	/**
	 * Gets all players tracking a block position in a server world.
	 *
	 * @param world the server world
	 * @param pos   the block position
	 * @return the players tracking the block position
	 */
	public static Collection<class_3222> tracking(class_3218 world, class_2338 pos) {
		Objects.requireNonNull(pos, "BlockPos cannot be null");

		return tracking(world, new class_1923(pos));
	}

	/**
	 * Gets all players around a position in a world.
	 *
	 * <p>The distance check is done in the three-dimensional space instead of in the horizontal plane.
	 *
	 * @param world  the world
	 * @param pos the position
	 * @param radius the maximum distance from the position in blocks
	 * @return the players around the position
	 */
	public static Collection<class_3222> around(class_3218 world, class_243 pos, double radius) {
		double radiusSq = radius * radius;

		return world(world)
				.stream()
				.filter((p) -> p.method_5707(pos) <= radiusSq)
				.collect(Collectors.toList());
	}

	/**
	 * Gets all players around a position in a world.
	 *
	 * <p>The distance check is done in the three-dimensional space instead of in the horizontal plane.
	 *
	 * @param world  the world
	 * @param pos    the position (can be a block pos)
	 * @param radius the maximum distance from the position in blocks
	 * @return the players around the position
	 */
	public static Collection<class_3222> around(class_3218 world, class_2382 pos, double radius) {
		double radiusSq = radius * radius;

		return world(world)
				.stream()
				.filter((p) -> p.method_5649(pos.method_10263(), pos.method_10264(), pos.method_10260()) <= radiusSq)
				.collect(Collectors.toList());
	}

	private PlayerLookup() {
	}
}

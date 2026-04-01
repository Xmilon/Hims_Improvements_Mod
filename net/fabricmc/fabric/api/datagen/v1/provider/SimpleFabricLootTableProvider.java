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

package net.fabricmc.fabric.api.datagen.v1.provider;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;
import net.minecraft.class_173;
import net.minecraft.class_176;
import net.minecraft.class_7225;
import net.minecraft.class_7403;

/**
 * Extend this class and implement {@link #method_10399}. Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class SimpleFabricLootTableProvider implements FabricLootTableProvider {
	protected final FabricDataOutput output;
	private final CompletableFuture<class_7225.class_7874> registryLookup;
	protected final class_176 contextType;

	public SimpleFabricLootTableProvider(FabricDataOutput output, CompletableFuture<class_7225.class_7874> registryLookup, class_176 contextType) {
		this.output = output;
		this.registryLookup = registryLookup;
		this.contextType = contextType;
	}

	@Override
	public CompletableFuture<?> method_10319(class_7403 writer) {
		return FabricLootTableProviderImpl.run(writer, this, contextType, output, registryLookup);
	}

	@Override
	public String method_10321() {
		return Objects.requireNonNull(class_173.field_1178.inverse().get(contextType), "Could not get id for loot context type") + " Loot Table";
	}
}

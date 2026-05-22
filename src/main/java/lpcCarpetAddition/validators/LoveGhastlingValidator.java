package lpcCarpetAddition.validators;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import lpcCarpetAddition.mixin.accessors.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class LoveGhastlingValidator extends Validator<Integer> {
	public static LootItem getGhastlingLootItem() { return ghastlingLootItem; }
	public static FoodProperties getGhastlingFoodProperties() { return ghastlingFoodProperties; }
	public static Consumable getGhastlingConsumable() { return ghastlingConsumable; }
	
	public static void init() {
		LootTableEvents.MODIFY.register(new LootTableModifier());
		ServerLifecycleEvents.SERVER_STARTED.register(new ServerStartedCallback());
	}
	
	private static LootItem ghastlingLootItem;
	private static final FoodProperties ghastlingFoodProperties = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build();
	private static final Consumable ghastlingConsumable = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 0))).build();
	private static DataComponentMap ghastlingDefaultDataComponentMap;
	private static MinecraftServer server;
	
	private static class ServerStartedCallback implements ServerLifecycleEvents.ServerStarted {
		@Override public void onServerStarted(@NonNull MinecraftServer minecraftServer) {
			ghastlingDefaultDataComponentMap = Items.DRIED_GHAST.components();
			server = minecraftServer;
		}
	}
	
	@Override public Integer validate(@Nullable CommandSourceStack source, CarpetRule<Integer> changingRule, Integer newValue, String userInput) {
		Holder.Reference<Item> holder = ((ItemAccessor)Items.DRIED_GHAST).getBuiltInRegistryHolder();
		int oldValue = changingRule.value();
		if((oldValue < 0) != (newValue < 0)) {
			FuelValuesAccessor fuelValues = ((FuelValuesAccessor) ((MinecraftServerAccessor)server).getFuelValues());
			Object2IntSortedMap<Item> items = fuelValues.getValues();
			if(newValue < 0) {
				holder.bindComponents(
					DataComponentMap.builder().addAll(ghastlingDefaultDataComponentMap)
						.set(DataComponents.FOOD, LoveGhastlingValidator.getGhastlingFoodProperties())
						.set(DataComponents.CONSUMABLE, LoveGhastlingValidator.getGhastlingConsumable())
						.build()
				);
				try {
					items.put(Items.DRIED_GHAST, 100);
				} catch (UnsupportedOperationException e) {
					Object2IntLinkedOpenHashMap<Item> newMap = new Object2IntLinkedOpenHashMap<>(items);
					newMap.put(Items.DRIED_GHAST, 100);
					fuelValues.setValues(newMap);
				}
			}
			else {
				holder.bindComponents(ghastlingDefaultDataComponentMap);
				try {
					items.removeInt(Items.DRIED_GHAST);
				} catch (UnsupportedOperationException e) {
					Object2IntLinkedOpenHashMap<Item> newMap = new Object2IntLinkedOpenHashMap<>(items);
					newMap.removeInt(Items.DRIED_GHAST);
					fuelValues.setValues(newMap);
				}
			}
		}
		return newValue;
	}
	
	private static class LootTableModifier implements LootTableEvents.Modify {
		@Override public void modifyLootTable(@NonNull ResourceKey<LootTable> key, LootTable.@NonNull Builder tableBuilder, @NonNull LootTableSource source, HolderLookup.@NonNull Provider holder) {
			if (key == BuiltInLootTables.PIGLIN_BARTERING && source.isBuiltin()) {
				LootTableAccessor.BuilderAccessor accessor = (LootTableAccessor.BuilderAccessor) tableBuilder;
				for(var pool : accessor.getPools().build()) {
					for(var entry : pool.entries) {
						if(entry instanceof LootItem lootItem) {
							LootItemAccessor itemAccessor = (LootItemAccessor) lootItem;
							if(itemAccessor.getItem().value() == Items.DRIED_GHAST)
								ghastlingLootItem = lootItem;
						}
					}
				}
			}
		}
	}
}

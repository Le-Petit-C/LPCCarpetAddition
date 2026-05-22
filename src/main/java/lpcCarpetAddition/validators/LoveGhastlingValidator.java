package lpcCarpetAddition.validators;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import lpcCarpetAddition.mixin.accessors.ItemAccessor;
import lpcCarpetAddition.mixin.accessors.LootItemAccessor;
import lpcCarpetAddition.mixin.accessors.LootTableAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
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
		ServerLifecycleEvents.SERVER_STARTED.register(_ -> ghastlingDefaultDataComponentMap = Items.DRIED_GHAST.components());
	}
	
	private static LootItem ghastlingLootItem;
	private static final FoodProperties ghastlingFoodProperties = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build();
	private static final Consumable ghastlingConsumable = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 0))).build();
	private static DataComponentMap ghastlingDefaultDataComponentMap;
	
	@Override public Integer validate(@Nullable CommandSourceStack source, CarpetRule<Integer> changingRule, Integer newValue, String userInput) {
		Holder.Reference<Item> holder = ((ItemAccessor)Items.DRIED_GHAST).getBuiltInRegistryHolder();
		int oldValue = changingRule.value();
		if((oldValue < 0) != (newValue < 0)) {
			if(newValue < 0) {
				holder.bindComponents(
					DataComponentMap.builder().addAll(ghastlingDefaultDataComponentMap)
						.set(DataComponents.FOOD, LoveGhastlingValidator.getGhastlingFoodProperties())
						.set(DataComponents.CONSUMABLE, LoveGhastlingValidator.getGhastlingConsumable())
						.build()
				);
			}
			else holder.bindComponents(ghastlingDefaultDataComponentMap);
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

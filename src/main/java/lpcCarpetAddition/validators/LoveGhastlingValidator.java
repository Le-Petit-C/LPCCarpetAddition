package lpcCarpetAddition.validators;

import lpcCarpetAddition.mixin.accessors.LootItemAccessor;
import lpcCarpetAddition.mixin.accessors.LootTableAccessor;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.jspecify.annotations.NonNull;

public class LoveGhastlingValidator{
	public static LootItem getGhastlingLootItem() { return ghastlingLootItem; }
	public static FoodProperties getGhastlingFoodProperties() { return ghastlingFoodProperties; }
	public static Consumable getGhastlingConsumable() { return ghastlingConsumable; }
	
	public static void init() { LootTableEvents.MODIFY.register(new LootTableModifier()); }
	
	private static LootItem ghastlingLootItem;
	private static final FoodProperties ghastlingFoodProperties = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build();
	private static final Consumable ghastlingConsumable = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 0))).build();
	
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

package com.blakebr0.morebuckets.init;

import com.blakebr0.morebuckets.MoreBuckets;
import com.blakebr0.morebuckets.crafting.ingredient.FluidBucketIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, MoreBuckets.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<FluidBucketIngredient>> FLUID_BUCKET = REGISTRY.register("fluid_bucket", () -> new IngredientType<>(FluidBucketIngredient.CODEC));
}

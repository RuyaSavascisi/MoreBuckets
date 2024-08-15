package com.blakebr0.morebuckets.crafting;

import com.blakebr0.cucumber.helper.RecipeHelper;
import com.blakebr0.morebuckets.config.ModConfigs;
import com.blakebr0.morebuckets.crafting.ingredient.FluidBucketIngredient;
import com.blakebr0.morebuckets.item.MoreBucketItem;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;

public class RecipeFixer implements ResourceManagerReloadListener {
    public static final ArrayList<MoreBucketItem> VALID_BUCKETS = new ArrayList<>();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        if (!ModConfigs.ENABLE_RECIPE_FIXER.get())
            return;

        VALID_BUCKETS.clear();

        for (var bucket : MoreBucketItem.BUCKETS) {
            if (bucket.isEnabled()) {
                VALID_BUCKETS.add(bucket);
            }
        }

        var recipes = RecipeHelper.getAllRecipes();

        for (var recipe : recipes) {
            var ingredients = recipe.value().getIngredients();

            for (int i = 0; i < ingredients.size(); i++) {
                var ingredient = ingredients.get(i);

                if (ingredient.isCustom()) {
                    assert ingredient.getCustomIngredient() != null;

                    var hasFluidIngredient = ingredient.getCustomIngredient().getItems().anyMatch(stack -> {
                        var tank = stack.getCapability(Capabilities.FluidHandler.ITEM);
                        return tank != null && !tank.getFluidInTank(0).isEmpty();
                    });

                    if (hasFluidIngredient) {
                        ingredients.set(i, FluidBucketIngredient.of(ingredient));
                    }
                } else {
                    for (var value : ingredient.getValues()) {
                        // we want to avoid initializing tag ingredients
                        if (value instanceof Ingredient.ItemValue) {
                            for (var stack : value.getItems()) {
                                var item = stack.getItem();
                                if (item instanceof BucketItem || item instanceof MilkBucketItem || item instanceof IFluidHandler) {
                                    ingredients.set(i, FluidBucketIngredient.of(ingredient));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(this);
    }
}

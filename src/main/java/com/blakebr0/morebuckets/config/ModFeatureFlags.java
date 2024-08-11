package com.blakebr0.morebuckets.config;

import com.blakebr0.cucumber.util.FeatureFlag;
import com.blakebr0.cucumber.util.FeatureFlags;
import com.blakebr0.morebuckets.MoreBuckets;

@FeatureFlags
public final class ModFeatureFlags {
    public static final FeatureFlag RECIPE_FIXER = FeatureFlag.create(MoreBuckets.resource("recipe_fixer"), ModConfigs.ENABLE_RECIPE_FIXER);
}

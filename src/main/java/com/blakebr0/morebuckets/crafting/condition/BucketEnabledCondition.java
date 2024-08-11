package com.blakebr0.morebuckets.crafting.condition;

import com.blakebr0.morebuckets.lib.ModBuckets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public class BucketEnabledCondition implements ICondition {
    public static final MapCodec<BucketEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("bucket").forGetter(condition -> condition.bucket)
            ).apply(builder, BucketEnabledCondition::new)
    );

    private final ResourceLocation bucket;

    public BucketEnabledCondition(ResourceLocation bucket) {
        this.bucket = bucket;
    }

    @Override
    public boolean test(IContext context) {
        var bucket = ModBuckets.ALL.get(this.bucket);
        return bucket != null && bucket.isEnabled();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}

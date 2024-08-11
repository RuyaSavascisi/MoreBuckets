package com.blakebr0.morebuckets.handler;

import com.blakebr0.morebuckets.bucket.BucketFluidHandlerItemStack;
import com.blakebr0.morebuckets.item.MoreBucketItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class RegisterCapabilityHandler {
    @SubscribeEvent
    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        for (var bucket : MoreBucketItem.BUCKETS) {
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, unused) -> new BucketFluidHandlerItemStack(stack, bucket.getCapacity()), bucket);
        }
    }
}

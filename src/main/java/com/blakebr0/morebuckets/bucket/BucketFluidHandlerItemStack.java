package com.blakebr0.morebuckets.bucket;

import com.blakebr0.cucumber.helper.FluidHelper;
import com.blakebr0.morebuckets.init.ModDataComponentTypes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

public class BucketFluidHandlerItemStack extends FluidHandlerItemStack {
    public BucketFluidHandlerItemStack(ItemStack container, int capacity) {
        super(ModDataComponentTypes.BUCKET_CONTENT, container, capacity);
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        var amount = FluidHelper.toBuckets(resource.getAmount());
        if (amount != resource.getAmount()) {
            resource = resource.copyWithAmount(amount);
        }

        return super.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        var amount = FluidHelper.toBuckets(maxDrain);
        if (amount != maxDrain) {
            maxDrain = amount;
        }

        return super.drain(maxDrain, action);
    }
}

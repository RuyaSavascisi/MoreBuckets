package com.blakebr0.morebuckets.item;

import com.blakebr0.cucumber.helper.FluidHelper;
import com.blakebr0.cucumber.helper.StackHelper;
import com.blakebr0.cucumber.item.BaseItem;
import com.blakebr0.cucumber.util.Formatting;
import com.blakebr0.morebuckets.bucket.Bucket;
import com.blakebr0.morebuckets.init.ModDataComponentTypes;
import com.blakebr0.morebuckets.lib.ModTooltips;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class MoreBucketItem extends BaseItem {
    public static final List<MoreBucketItem> BUCKETS = new ArrayList<>();

    private final Bucket bucket;

    public MoreBucketItem(Bucket bucket) {
        super(p -> p
                .stacksTo(1)
                .component(ModDataComponentTypes.BUCKET_CONTENT, SimpleFluidContent.EMPTY)
        );
        this.bucket = bucket;

        DispenserBlock.registerBehavior(this, new DispenserBehavior());

        BUCKETS.add(this);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return FluidHelper.getFluidAmount(stack) > 0;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        var copy = new ItemStack(this);
        copy.applyComponents(stack.getComponents());

        var tank = copy.getCapability(Capabilities.FluidHandler.ITEM);
        if (tank != null) {
            tank.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
        }

        return copy;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int capacity = this.bucket.getCapacity();
        int stored = capacity - FluidHelper.getFluidAmount(stack);

        return Math.round(13.0F - stored * 13.0F / (float) capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int capacity = this.bucket.getCapacity();
        int stored = FluidHelper.getFluidAmount(stack);

        float f = Math.max(0.0F, (float) stored / (float) capacity);

        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return this.bucket.getCapacity() > FluidType.BUCKET_VOLUME;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isMilkBucket(stack) ? UseAnim.DRINK : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return isMilkBucket(stack) ? 32 : 0;
    }

    @Override
    public int getBurnTime(ItemStack stack, RecipeType<?> type) {
        var fluid = FluidHelper.getFluidFromStack(stack);
        if (fluid.is(Fluids.LAVA)) {
            if (FluidHelper.getFluidAmount(stack) >= FluidType.BUCKET_VOLUME) {
                return 20000;
            }
        }

        return super.getBurnTime(stack, type);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var capacity = Formatting.number(this.bucket.getBuckets());
        int buckets = FluidHelper.getFluidAmount(stack) / FluidType.BUCKET_VOLUME;
        var fluid = FluidHelper.getFluidFromStack(stack);

        if (fluid.isEmpty()) {
            tooltip.add(ModTooltips.BUCKETS.args(buckets, capacity, ModTooltips.EMPTY.build()).build());
        } else {
            tooltip.add(ModTooltips.BUCKETS.args(buckets, capacity, fluid.getHoverName()).build());
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (isMilkBucket(stack)) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }

        var tank = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (tank == null) {
            return InteractionResultHolder.fail(stack);
        }

        var pickup = this.tryPickupFluid(stack, level, player);
        if (pickup.getResult() == InteractionResult.SUCCESS) {
            return pickup;
        } else {
            var fluid = FluidHelper.getFluidFromStack(stack);
            if (fluid != null && fluid.getAmount() >= FluidType.BUCKET_VOLUME) {
                return this.tryPlaceFluid(stack, level, player, hand);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        }
    }

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
		if (entity instanceof Cow cow && !cow.isBaby()) {
            var tank = stack.getCapability(Capabilities.FluidHandler.ITEM);

            if (tank != null && tank.fill(new FluidStack(NeoForgeMod.MILK.get(), FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE) > 0) {
                player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
		}

		return InteractionResult.PASS;
	}

    @Override // copied from MilkBucketItem#finishUsingItem
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        if (!level.isClientSide) {
            entity.removeEffectsCuredBy(EffectCures.MILK);
        }

        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            // change: instead of shrinking the stack we drain the fluid
            var tank = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (tank != null) {
                tank.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
            }
        }

        return stack;
    }

    public int getCapacity() {
        return this.bucket.getCapacity();
    }

    public int getSpaceLeft(ItemStack stack) {
        return this.bucket.getCapacity() - FluidHelper.getFluidAmount(stack);
    }

    public boolean isEnabled() {
        return this.bucket.isEnabled();
    }

    private InteractionResultHolder<ItemStack> tryPlaceFluid(ItemStack stack, Level level, Player player, InteractionHand hand) {
        if (FluidHelper.getFluidAmount(stack) < FluidType.BUCKET_VOLUME)
            return InteractionResultHolder.pass(stack);

        var trace = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (trace.getType() != HitResult.Type.BLOCK)
            return InteractionResultHolder.pass(stack);

        var pos = trace.getBlockPos();
        if (level.mayInteract(player, pos)) {
            var targetPos = pos.relative(trace.getDirection());

            if (player.mayUseItemAt(targetPos, trace.getDirection().getOpposite(), stack)) {
                var result = FluidUtil.tryPlaceFluid(player, level, hand, targetPos, stack, FluidHelper.getFluidFromStack(stack).copyWithAmount(FluidType.BUCKET_VOLUME));
                if (result.isSuccess() && !player.getAbilities().instabuild) {
                    if (!level.isClientSide()) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, stack);
                    }

                    return InteractionResultHolder.success(result.getResult());
                }
            }
        }

        return InteractionResultHolder.fail(stack);
    }

    private InteractionResultHolder<ItemStack> tryPickupFluid(ItemStack stack, Level level, Player player) {
        if (this.getSpaceLeft(stack) < FluidType.BUCKET_VOLUME)
            return InteractionResultHolder.pass(stack);

        var trace = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (trace.getType() != HitResult.Type.BLOCK)
            return InteractionResultHolder.pass(stack);

        var pos = trace.getBlockPos();
        if (level.mayInteract(player, pos)) {
            var direction = trace.getDirection();
            if (player.mayUseItemAt(pos, direction, stack)) {
                var result = FluidUtil.tryPickUpFluid(stack, player, level, pos, direction);

                if (result.isSuccess() && !player.getAbilities().instabuild) {
                    if (!level.isClientSide()) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, stack);
                    }

                    return InteractionResultHolder.success(result.getResult());
                }
            }
        }

        return InteractionResultHolder.fail(stack);
    }

    private static boolean isMilkBucket(ItemStack stack) {
        return FluidHelper.getFluidFromStack(stack).getFluid() == NeoForgeMod.MILK.get();
    }

    private static class DispenserBehavior extends OptionalDispenseItemBehavior {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            var level = source.level();
            var facing = source.state().getValue(DispenserBlock.FACING);
            var pos = source.pos().relative(facing);

            var action = FluidUtil.tryPickUpFluid(stack, null, level, pos, facing.getOpposite());
            var resultStack = action.getResult();

            if (!action.isSuccess() || resultStack.isEmpty()) {
                var singleStack = StackHelper.withSize(stack, 1, false);

                var fluidHandler = FluidUtil.getFluidHandler(singleStack);
                if (fluidHandler.isEmpty()) return super.execute(source, stack);

                var fluidStack = fluidHandler.get().drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
                var result = !fluidStack.isEmpty() ? FluidUtil.tryPlaceFluid(null, level, InteractionHand.MAIN_HAND, pos, stack, fluidStack) : FluidActionResult.FAILURE;

                if (result.isSuccess()) {
                    var drainedStack = result.getResult();

                    if (drainedStack.getCount() == 1) {
                        return drainedStack;
                    } else if (!drainedStack.isEmpty() && !source.blockEntity().insertItem(drainedStack).isEmpty()) {
                        this.dispense(source, drainedStack);
                    }

                    return StackHelper.shrink(drainedStack, 1, false);
                } else {
                    return this.dispense(source, stack);
                }
            } else {
                if (stack.getCount() == 1) {
                    return resultStack;
                } else if (!source.blockEntity().insertItem(resultStack).isEmpty()) {
                    this.dispense(source, resultStack);
                }
            }

            return resultStack;
        }
    }
}
package com.codehusky.huskycrates.crate.virtual;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Predicate;

public enum HideFlags {

    HideEnchantments((itemStack) -> itemStack.offer(Keys.HIDE_ENCHANTMENTS, true), (itemStack) -> itemStack.get(Keys.HIDE_ENCHANTMENTS).orElse(false)),
    HideAttributeModifiers((itemStack) -> itemStack.offer(Keys.HIDE_ATTRIBUTES, true), (itemStack) -> itemStack.get(Keys.HIDE_ATTRIBUTES).orElse(false)),
    HideUnbreakable((itemStack) -> itemStack.offer(Keys.HIDE_UNBREAKABLE, true), (itemStack) -> itemStack.get(Keys.HIDE_UNBREAKABLE).orElse(false)),
    CanDestroy((itemStack) -> itemStack.offer(Keys.HIDE_CAN_DESTROY, true), (itemStack) -> itemStack.get(Keys.HIDE_CAN_DESTROY).orElse(false)),
    CanPlaceOn((itemStack) -> itemStack.offer(Keys.HIDE_CAN_PLACE, true), (itemStack) -> itemStack.get(Keys.HIDE_CAN_PLACE).orElse(false)),
    Miscellaneous((itemStack) -> itemStack.offer(Keys.HIDE_MISCELLANEOUS, true), (itemStack) -> itemStack.get(Keys.HIDE_MISCELLANEOUS).orElse(false));

    private Consumer<ItemStack> applyConsumer;
    private Predicate<ItemStack> hasFlag;

    HideFlags(Consumer<ItemStack> applyConsumer, Predicate<ItemStack> hasFlag) {
        this.applyConsumer = applyConsumer;
        this.hasFlag = hasFlag;
    }

    public void apply(ItemStack itemStack) {
        this.applyConsumer.accept(itemStack);
    }

    public boolean hasFlag(ItemStack itemStack) {
        return hasFlag.test(itemStack);
    }

}
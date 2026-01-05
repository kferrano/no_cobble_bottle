/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package com.hardrock.nocobbottle.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.StackWalker;

@Mixin(Player.class)
public abstract class PlayerAddItemGuard {

    private static final StackWalker SW =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private static final String[] COBBLE_PREFIXES = {
            "com.cobblemon.mod.common.item.medicine",
            "com.cobblemon.mod.common.item",
            "com.cobblemon.mod.common"
    };

    @Inject(method = "addItem(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void nocobblebottle$blockCobblemonBottle(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack != null && stack.is(Items.GLASS_BOTTLE) && isFromCobblemonStack()) {
            stack.shrink(stack.getCount());
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    private static boolean isFromCobblemonStack() {
        return SW.walk(frames ->
                frames.anyMatch(f -> {
                    String cn = f.getClassName();
                    for (String p : COBBLE_PREFIXES) if (cn.startsWith(p)) return true;
                    return false;
                })
        );
    }

}

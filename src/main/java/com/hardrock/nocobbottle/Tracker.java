/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package com.hardrock.nocobbottle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;

public class Tracker {
    private static final ResourceLocation GLASS_BOTTLE_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "glass_bottle");

    private static final List<ResourceLocation> HEAL_ITEMS = List.of(
            ResourceLocation.fromNamespaceAndPath("cobblemon", "potion"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "super_potion"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "hyper_potion"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "max_potion"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "full_restore"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "full_heal"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "antidote"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "ether"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "elixir")
    );

    private static class State {
        int lastBottles = 0;
        Map<ResourceLocation, Integer> lastHealCounts = new HashMap<>();
        int windowTicks = 0;
    }

    private final Map<UUID, State> states = new HashMap<>();

    public void tick(Player p) {
        final UUID id = p.getUUID();
        State s = states.computeIfAbsent(id, k -> initState(p));

        int bottlesNow = count(p, GLASS_BOTTLE_ID);

        boolean healUsed = false;
        for (ResourceLocation rl : HEAL_ITEMS) {
            int now = count(p, rl);
            int before = s.lastHealCounts.getOrDefault(rl, 0);
            if (now < before) healUsed = true;
            s.lastHealCounts.put(rl, now);
        }
        if (healUsed) {
            s.windowTicks = Math.max(s.windowTicks, 12);
        }

        if (s.windowTicks > 0) {
            if (bottlesNow > s.lastBottles) {
                int delta = bottlesNow - s.lastBottles;
                removeFromInventory(p, GLASS_BOTTLE_ID, delta);
                bottlesNow = count(p, GLASS_BOTTLE_ID);
            }
            s.windowTicks--;
        }

        s.lastBottles = bottlesNow;
    }

    private State initState(Player p) {
        State s = new State();
        s.lastBottles = count(p, GLASS_BOTTLE_ID);
        for (ResourceLocation rl : HEAL_ITEMS) {
            s.lastHealCounts.put(rl, count(p, rl));
        }
        return s;
    }

    private static int count(Player p, ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null) return 0;
        int total = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack st = p.getInventory().getItem(i);
            if (!st.isEmpty() && st.is(item)) total += st.getCount();
        }
        return total;
    }

    private static void removeFromInventory(Player p, ResourceLocation id, int amount) {
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || amount <= 0) return;
        for (int i = 0; i < p.getInventory().getContainerSize() && amount > 0; i++) {
            ItemStack st = p.getInventory().getItem(i);
            if (!st.isEmpty() && st.is(item)) {
                int take = Math.min(st.getCount(), amount);
                st.shrink(take);
                amount -= take;
            }
        }
    }
}

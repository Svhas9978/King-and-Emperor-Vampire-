package ru.darkthrone.gameplay;

import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.server.MinecraftServer;

public final class VampireProgress {
    private VampireProgress() {}

    public static boolean isVampireLord5(ServerPlayerEntity p) {
        return VampirismBridge.getLordLevel(p) >= 5;
    }

    /** True when every advancement under vampirism:vampire/ is completed. */
    public static boolean allVampireAdvancements(ServerPlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null) return false;
        int total = 0;
        int done = 0;
        for (Advancement a : server.getAdvancements().getAllAdvancements()) {
            ResourceLocation id = a.getId();
            if (!"vampirism".equals(id.getNamespace()) || !id.getPath().startsWith("vampire/")) continue;
            total++;
            if (p.getAdvancements().getOrStartProgress(a).isDone()) done++;
        }
        return total > 0 && done == total;
    }
}

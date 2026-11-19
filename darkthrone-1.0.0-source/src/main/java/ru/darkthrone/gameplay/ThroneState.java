package ru.darkthrone.gameplay;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

/** Title state kept in the player's persistent NBT: 0 normal, 1 King, 2 Emperor, 3 crowned. */
public final class ThroneState {
    private ThroneState() {}

    public static final String ROOT = "DarkThrone";
    private static final String TITLE = "Title";

    public static int get(PlayerEntity p) {
        return p.getPersistentData().getCompound(ROOT).getInt(TITLE);
    }

    public static void set(PlayerEntity p, int value) {
        CompoundNBT root = p.getPersistentData().getCompound(ROOT);
        root.putInt(TITLE, value);
        p.getPersistentData().put(ROOT, root);
    }

    public static boolean king(PlayerEntity p) { return get(p) >= 1; }
    public static boolean emperor(PlayerEntity p) { return get(p) >= 2; }
    public static boolean crowned(PlayerEntity p) { return get(p) >= 3; }

    /** Cooldown timestamps (game time) per ability id, stored next to the title. */
    public static long cooldownUntil(PlayerEntity p, String ability) {
        return p.getPersistentData().getCompound(ROOT).getLong("cd_" + ability);
    }

    public static void setCooldown(PlayerEntity p, String ability, long untilGameTime) {
        CompoundNBT root = p.getPersistentData().getCompound(ROOT);
        root.putLong("cd_" + ability, untilGameTime);
        p.getPersistentData().put(ROOT, root);
    }

    /** Copy everything to the new player object after death / returning from the End. */
    public static void copy(PlayerEntity from, PlayerEntity to) {
        CompoundNBT old = from.getPersistentData().getCompound(ROOT);
        if (!old.isEmpty()) {
            to.getPersistentData().put(ROOT, old.copy());
        }
    }
}

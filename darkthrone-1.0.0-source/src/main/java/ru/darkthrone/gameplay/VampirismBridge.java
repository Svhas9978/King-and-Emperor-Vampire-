package ru.darkthrone.gameplay;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * All access to Vampirism goes through here, by reflection, so this mod compiles
 * without Vampirism on the classpath and a wrong method name never breaks the build.
 * Every failure is logged once (search the log for "[DarkThrone]").
 */
public final class VampirismBridge {
    private VampirismBridge() {}

    private static final Logger LOG = LogManager.getLogger("DarkThrone");
    private static final Set<String> REPORTED = new HashSet<String>();

    private static void report(String key, String message, Throwable t) {
        if (REPORTED.add(key)) {
            LOG.warn("[DarkThrone] Vampirism bridge: " + message, t);
        }
    }

    /** Unwraps LazyOptional / Optional / plain objects. */
    private static Object unwrap(Object o) {
        if (o instanceof LazyOptional) return ((LazyOptional<?>) o).orElse(null);
        if (o instanceof Optional) return ((Optional<?>) o).orElse(null);
        return o;
    }

    private static Method findMethod(Class<?> c, String name, int args) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name) && m.getParameterTypes().length == args) return m;
        }
        return null;
    }

    private static Object factionPlayerHandler(PlayerEntity p) throws Exception {
        Class<?> api = Class.forName("de.teamlapen.vampirism.api.VampirismAPI");
        Method m = findMethod(api, "getFactionPlayerHandler", 1);
        if (m == null) throw new NoSuchMethodException("VampirismAPI.getFactionPlayerHandler");
        return unwrap(m.invoke(null, p));
    }

    /** Lord level of the player, or 0 if unknown. */
    public static int getLordLevel(PlayerEntity p) {
        try {
            Object handler = factionPlayerHandler(p);
            if (handler == null) return 0;
            Method m = findMethod(handler.getClass(), "getLordLevel", 0);
            if (m == null) throw new NoSuchMethodException("getLordLevel");
            return ((Number) m.invoke(handler)).intValue();
        } catch (Throwable t) {
            report("lord", "could not read lord level", t);
            return 0;
        }
    }

    /** Fill the player's blood (vampires only). Returns false if it could not be done. */
    public static boolean fillBlood(PlayerEntity p, int amount) {
        try {
            Object handler = factionPlayerHandler(p);
            if (handler == null) return false;
            Method cur = findMethod(handler.getClass(), "getCurrentFactionPlayer", 0);
            if (cur == null) throw new NoSuchMethodException("getCurrentFactionPlayer");
            Object fp = unwrap(cur.invoke(handler));
            if (fp == null) return false;
            Method drink = findMethod(fp.getClass(), "drinkBlood", 2);
            if (drink == null) {
                report("drink", "drinkBlood(int,float) not found on " + fp.getClass().getName(), null);
                return false;
            }
            drink.invoke(fp, amount, 0f);
            return true;
        } catch (Throwable t) {
            report("blood", "could not restore blood", t);
            return false;
        }
    }
}

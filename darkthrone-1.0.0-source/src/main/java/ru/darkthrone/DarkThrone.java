package ru.darkthrone;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import ru.darkthrone.registry.DTRegistry;

@Mod(DarkThrone.MOD_ID)
public class DarkThrone {
    public static final String MOD_ID = "darkthrone";

    public DarkThrone() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        DTRegistry.ITEMS.register(bus);
        DTRegistry.BLOCKS.register(bus);
        DTRegistry.EFFECTS.register(bus);
        DTRegistry.ENTITIES.register(bus);
    }
}

package ru.darkthrone;

import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import ru.darkthrone.entity.XardrotEntity;
import ru.darkthrone.network.DTNetwork;
import ru.darkthrone.registry.DTRegistry;

@Mod.EventBusSubscriber(modid = DarkThrone.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModSetup {
    private ModSetup() {}

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            DTNetwork.init();
            GlobalEntityTypeAttributes.put(DTRegistry.XARDROT.get(), XardrotEntity.createAttributes().build());
        });
    }
}

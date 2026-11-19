package ru.darkthrone.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraftforge.event.TickEvent;
import ru.darkthrone.DarkThrone;
import ru.darkthrone.gameplay.Abilities;
import ru.darkthrone.network.ActionMessage;
import ru.darkthrone.network.DTNetwork;
import ru.darkthrone.registry.DTRegistry;

@Mod.EventBusSubscriber(modid = DarkThrone.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    public static final KeyBinding PACK = new KeyBinding("key.darkthrone.pack", GLFW.GLFW_KEY_G, "key.categories.darkthrone");
    public static final KeyBinding ROAR = new KeyBinding("key.darkthrone.roar", GLFW.GLFW_KEY_H, "key.categories.darkthrone");
    public static final KeyBinding ECLIPSE = new KeyBinding("key.darkthrone.eclipse", GLFW.GLFW_KEY_J, "key.categories.darkthrone");

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent e) {
        ClientRegistry.registerKeyBinding(PACK);
        ClientRegistry.registerKeyBinding(ROAR);
        ClientRegistry.registerKeyBinding(ECLIPSE);
        RenderingRegistry.registerEntityRenderingHandler(DTRegistry.XARDROT.get(), XardrotRenderer::new);
        MinecraftForge.EVENT_BUS.register(KeyHandler.class);
    }

    /** Forge-bus handler: sends the ability request to the server. */
    public static final class KeyHandler {
        private KeyHandler() {}

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            while (PACK.consumeClick()) DTNetwork.CHANNEL.sendToServer(new ActionMessage(Abilities.PACK));
            while (ROAR.consumeClick()) DTNetwork.CHANNEL.sendToServer(new ActionMessage(Abilities.ROAR));
            while (ECLIPSE.consumeClick()) DTNetwork.CHANNEL.sendToServer(new ActionMessage(Abilities.ECLIPSE));
        }
    }
}

package ru.darkthrone.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.darkthrone.gameplay.Abilities;

import java.util.function.Supplier;

/** Client -> server: "use ability <id>". */
public class ActionMessage {
    private final int id;

    public ActionMessage(int id) {
        this.id = id;
    }

    public static void encode(ActionMessage m, PacketBuffer buf) {
        buf.writeVarInt(m.id);
    }

    public static ActionMessage decode(PacketBuffer buf) {
        return new ActionMessage(buf.readVarInt());
    }

    public static void handle(ActionMessage m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayerEntity p = c.getSender();
            if (p != null && m.id >= Abilities.PACK && m.id <= Abilities.ECLIPSE) {
                Abilities.use(p, m.id);
            }
        });
        c.setPacketHandled(true);
    }
}

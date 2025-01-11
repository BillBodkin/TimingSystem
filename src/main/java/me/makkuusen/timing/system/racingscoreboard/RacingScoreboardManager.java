package me.makkuusen.timing.system.racingscoreboard;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.makkuusen.timing.system.TimingSystem;
import me.makkuusen.timing.system.database.TSDatabase;
import me.makkuusen.timing.system.tplayer.TPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SuppressWarnings("UnstableApiUsage")
public class RacingScoreboardManager {
    public static final String CHANNEL = "racing_scoreboard:data";
    public static final int SUPPORTED_VERSION = 1;

    public static final int PACKET_ID_PROTOCOL_VERSION_C2S = 0;
    public static final int PACKET_ID_SCOREBOARD_TITLE_S2C = 1;
    public static final int PACKET_ID_REMOVE_SCOREBOARD_S2C = 2;
    public static final int PACKET_ID_RACE_SCOREBOARD_S2C = 3;
    public static final int PACKET_ID_TT_SESSION_SCOREBOARD_S2C = 4;

    public static void pluginMessageListener(@NotNull String channel, @NotNull Player player, byte[] message) {
        TPlayer tPlayer = TSDatabase.getPlayer(player.getUniqueId());

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        short packetID = in.readShort();
        if (packetID == PACKET_ID_PROTOCOL_VERSION_C2S) {
            // Define protocol version
            int version = in.readInt();

            if(version != SUPPORTED_VERSION){
                // Unsupported version, will set to null so server continues to send vanilla scoreboard data
                // Client can also send 0 to disable the mod (versions start from 1)
                tPlayer.setRacingScoreboardVersion(null);
                return;
            }

            tPlayer.setRacingScoreboardVersion(version);
        }
        else {
            // Unknown C2S packet, disable mod support for client to prevent unexpected behaviour
            tPlayer.setRacingScoreboardVersion(null);
        }
    }

    public static void sendPluginMessage(@NotNull Player player, int packetId, RacingScoreboardSendPacketFunc func) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeShort(packetId);
            func.populateDataOutputStream(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendPluginMessage(TimingSystem.getPlugin(), CHANNEL, b.toByteArray());
    }
}

package me.makkuusen.timing.system.racingscoreboard;

import me.makkuusen.timing.system.heat.Heat;
import me.makkuusen.timing.system.heat.Lap;
import me.makkuusen.timing.system.participant.Driver;
import me.makkuusen.timing.system.round.QualificationRound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class RacingScoreboardUtils {

    public static byte packBooleans(boolean[] booleans) {
        if (booleans.length > 8) {
            throw new IllegalArgumentException("Cannot pack more than 8 booleans into a byte");
        }

        byte result = 0;
        for (int i = 0; i < booleans.length; i++) {
            if (booleans[i]) {
                result |= (byte) (1 << i); // Set the i-th bit
            }
        }
        return result;
    }

    // Unpack booleans from a byte
    public static boolean[] unpackBooleans(byte packedByte) {
        boolean[] booleans = new boolean[8];
        for (int i = 0; i < 8; i++) {
            booleans[i] = (packedByte & (1 << i)) != 0; // Check the i-th bit
        }
        return booleans;
    }

    public static void setTitleRacingScoreboard(Player player, String title) {
        RacingScoreboardManager.sendPluginMessage(
            player,
            RacingScoreboardManager.PACKET_ID_SCOREBOARD_TITLE_S2C,
            (out) -> {
                out.writeChars(title);
            }
        );
    }

    public static void sendScoreboardForHeat(Heat heat, Player player, @Nullable Driver selfDriver){
        boolean qualificationRound = heat.getRound() instanceof QualificationRound;

        RacingScoreboardManager.sendPluginMessage(
            player,
            RacingScoreboardManager.PACKET_ID_RACE_SCOREBOARD_S2C,
            (out) -> {
                // Is Qualification Y/N
                if (qualificationRound) {
                    out.writeShort(1);
                }
                else{
                    out.writeShort(0);
                }

                Driver prevoiusDriver = null;
                for (Driver driver : heat.getLivePositions()) {
                    // UUID
                    UUID driverUuid = driver.getTPlayer().getPlayer().getUniqueId();
                    out.writeLong(driverUuid.getMostSignificantBits());
                    out.writeLong(driverUuid.getLeastSignificantBits());

                    // Team color
                    out.writeInt(driver.getTPlayer().getSettings().getTextColor().value());

                    // Position
                    if(driver.getPosition() == null) {
                        out.writeShort(0);
                    }
                    else{
                        out.writeShort(driver.getPosition());
                    }

                    // State
                    out.writeByte(RacingScoreboardUtils.packBooleans(new boolean[]{
                        driver.isRunning(),
                        driver.isFinished(),
                        driver.isDisqualified(),
                        driver.isInPit(driver.getTPlayer().getPlayer().getLocation()),
                        driver.getHeat().getFastestLapUUID() == driver.getTPlayer().getUniqueId()
                    }));

                    // Time gap
                    if(selfDriver != null) {
                        out.writeLong(driver.getTimeGap(selfDriver));
                    }
                    else if(prevoiusDriver != null) {
                        out.writeLong(driver.getTimeGap(prevoiusDriver));
                    }
                    else{
                        out.writeLong(0);
                    }

                    if (qualificationRound) {
                        // Best lap
                        Optional<Lap> bestLap = driver.getBestLap();
                        if (bestLap.isPresent()) {
                            out.writeLong(bestLap.get().getLapTime());
                        }
                    }

                    prevoiusDriver = driver;
                }
            }
        );
    }
}

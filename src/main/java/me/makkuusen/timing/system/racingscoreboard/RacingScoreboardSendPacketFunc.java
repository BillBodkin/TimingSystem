package me.makkuusen.timing.system.racingscoreboard;

import java.io.DataOutputStream;
import java.io.IOException;

public interface RacingScoreboardSendPacketFunc {
    void populateDataOutputStream(DataOutputStream out) throws IOException;
}

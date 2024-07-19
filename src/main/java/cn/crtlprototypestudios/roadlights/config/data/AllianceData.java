package cn.crtlprototypestudios.roadlights.config.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class AllianceData {
    private static final List<String> alliedPlayers = Collections.synchronizedList(new ArrayList<String>());

    public static void setAlliedPlayers(List<String> players) {
        alliedPlayers.clear();
        alliedPlayers.addAll(players);
    }

    public static boolean isAllied(String playerName) {
        return alliedPlayers.contains(playerName);
    }
}

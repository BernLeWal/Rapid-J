// License: Apache 2.0. See LICENSE file in root directory.
package rapid;

import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rapid.net.Network;
import rapid.net.port.MapToOneHotPort;
import rapid.net.port.OneHotPort;
import rapid.net.port.Port;
import rapid.net.port.PortFactory;
import rapid.util.Utils;

public class BoxingGame {

    private static final Logger LOG = LogManager.getLogger(BoxingGame.class);

    public static void main(String[] args) {
//        BoxingGame bitCountingTest = new BoxingGame(8 + "BitCounterBinary");
//        bitCountingTest.bitCountingTest(8, bitCountingTest.mlNetwork.createBinaryInput("", 8));

        BoxingGame boxingGame = new BoxingGame("Rules");
        boxingGame.learnRules();
        boxingGame.learnPlayers();
    }

    private final Network rulesNet;     // neural-network containing the learned game's rules
    private final Network playersNet;   // neural-network containig the learned enemy's players behaviour

    public BoxingGame(String name) {
        // create ML-Network
        rulesNet = new Network("Rules");
        playersNet = new Network("Players");
    }

    public boolean learnRules() {
        LOG.info("========== Started: " + rulesNet.name + " ==========");

        LOG.info("---------- PHASE 0: Initialisation ---------- ");
        int cycle = rulesNet.getCycles();
        rulesNet.addInput(PortFactory.createOneHot("InPos", 2, cycle));
        rulesNet.addInput(PortFactory.createOneHot("InAct", 1, cycle));
        rulesNet.addOutput(PortFactory.createOneHot("OutPos", 2, cycle));
        rulesNet.addOutput(PortFactory.createOneHot("OutAct", 1, cycle));

        LOG.info("---------- PHASE 1: Learning ---------- ");
        // create test-patterns and result-putterns
        int[][] inputPattern = new int[][]{
            {0, 0}, {0, 1}, {1, 0}, {1, 1}, {2, 0}, {2, 1},
            {0, 0}, {1, 0}, {2, 0} // this line contains multiple-correct results
        };
        int[][] outputPattern = new int[][]{
            {1, 1}, {0, 0}, {0, 1}, {1, 0}, {0, 1}, {2, 0},
            {2, 1}, {2, 1}, {1, 1} // this line contains multiple-correct results
        };

        long startMillis = System.currentTimeMillis();
        for (int i = 0; i < inputPattern.length; i++) {
            rulesNet.learn(inputPattern[i], outputPattern[i], null, true);
        }
        LOG.info(rulesNet.toString() + "\n" + rulesNet.dumpNetworkToString(false));

        LOG.info("---------- PHASE 2: Verification ---------- ");
        int successCount = 0;
        int failCount = 0;
        for (int i = 0; i < inputPattern.length; i++) {
            if (rulesNet.verify(inputPattern[i], null, (in, out) -> {
                return checkRules(in, out);
            })) {
                successCount++;
            } else {
                failCount++;
            }
        }

        // verfiy again to test mutliple-correct results
        for (int i = 0; i < inputPattern.length; i++) {
            if (rulesNet.verify(inputPattern[i], null, (in, out) -> {
                return checkRules(in, out);
            })) {
                successCount++;
            } else {
                failCount++;
            }
        }

        long stopMillis = System.currentTimeMillis();
        LOG.info(rulesNet.toString());
        if (failCount == 0) {
            LOG.info("OK - Tests succeeded=" + successCount + " failed=" + failCount + " duration=" + (stopMillis - startMillis) + " msec.");
        } else {
            LOG.error("FAILED - Tests succeeded=" + successCount + " failed=" + failCount + " duration=" + (stopMillis - startMillis) + " msec.");
        }
        LOG.info("========== Finished: " + rulesNet.name + " ==========");
        return (failCount == 0);
    }

    public boolean checkRules(Integer[] input, Integer[] output) {
        if (input[1] == 0 && output[1] == 1) {
            return (!Objects.equals(input[0], output[0]));
        } else if (input[1] == 1 && output[1] == 0) {
            return (Objects.equals(input[0], output[0]));
        } else {
            return false;
        }
    }

    public boolean learnPlayers() {
        LOG.info("========== Started: " + playersNet.name + " ==========");

        LOG.info("---------- PHASE 0: Initialisation ---------- ");
        int cycle = playersNet.getCycles();
        OneHotPort inPos = PortFactory.createOneHot("InPos", 2, cycle);
        OneHotPort inAct = PortFactory.createOneHot("InAct", 1, cycle);
        playersNet.addInput(PortFactory.createStream("InMoves", new Port[]{inPos, inAct}));
        MapToOneHotPort inPlayer = playersNet.addInput(PortFactory.createMapToOneHot("InPlayer"));
        OneHotPort outPos = PortFactory.createOneHot("OutPos", 2, cycle);
        OneHotPort outAct = PortFactory.createOneHot("OutAct", 1, cycle);
        playersNet.addOutput(PortFactory.createStream("OutMoves", new Port[]{outPos, outAct}));

        String[] playerNames = {
            "Boxer1",
            "StonedGuy",
            "ShyGuy",
            "StupidHitGuy",
            "HitGuy"
        };

        for (String playerName : playerNames) {
            inPlayer.createItem(playerName, playersNet.getCycles());
        }

        int[][][] playerMoves = new int[][][]{
            {{1, 1}, {2, 1}, {0, 1}, {0, 0}, {1, 0}, {1, 1}, {1, 0}, {2, 1}, {2, 0}, {0, 1}},   // Boxer1
            {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},   // StonedGuy
            {{1, 0}, {0, 0}, {2, 0}, {0, 0}, {1, 0}, {2, 0}, {0, 0}, {0, 0}, {2, 0}, {0, 0}},   // ShyGy
            {{1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}},   // StupidHitGuy
            {{1, 1}, {0, 1}, {2, 1}, {0, 1}, {1, 1}, {2, 1}, {0, 1}, {0, 1}, {2, 1}, {0, 1}},   // HitGuy
        };

        LOG.info("---------- PHASE 1: Learn & verify ---------- ");
        long startMillis = System.currentTimeMillis();
        int failCount = 0;

        for (int i = 0; i < playerNames.length; i++) {
        //int i=0; {
            failCount += learnAndVerifyPlayer(inPlayer, playerNames[i], playerMoves[i]);
        }

        long stopMillis = System.currentTimeMillis();
        if (failCount == 0) {
            LOG.info("ALL OK - duration=" + (stopMillis - startMillis) + " msec.");
        } else {
            LOG.error("FAILED - total-failed=" + failCount + " duration=" + (stopMillis - startMillis) + " msec.");
        }
        LOG.info(playersNet.toString());

        LOG.info("========== Finished: " + playersNet.name + " ==========");
        return failCount == 0;
    }

    private int learnAndVerifyPlayer(MapToOneHotPort inputPlayer, String name, int[][] playerMoves) {
        LOG.info("---------- 1.a) Learning player=" + name + " ---------- ");
        playersNet.clearPortValues();
        for (int[] playerMove : playerMoves) {
            //int[] results = rulesNet.
            int[] ownMove = rulesNet.query(playerMove, null);
            LOG.info("Player " + name + " does " + Utils.intArrayToString(playerMove) + " - my answer is " + Utils.intArrayToString(ownMove));

            playersNet.learn(playerMove, ownMove,
                    (bfp, cycle) -> {
                        inputPlayer.setItem(name, bfp, cycle);
                    }, false);
        }
        LOG.info(playersNet.toString() + "\n" + playersNet.dumpNetworkToString(false));

        LOG.info("---------- 1.b) Verify player=" + name + " ---------- ");
        int failCount = verifyPlayer(1, inputPlayer, name, playerMoves);

        LOG.info("---------- 1.c) Optimize ---------- ");
        playersNet.optimizeAll();
        // TODO check optimation why a singe player's input is not fully removed from the network
        LOG.info(playersNet.toString() + "\n" + playersNet.dumpNetworkToString(false));

        LOG.info("---------- 1.d) Verify player=" + name + " ---------- ");
        failCount += verifyPlayer(2, inputPlayer, name, playerMoves);

        return failCount;
    }

    private int verifyPlayer(int nr, MapToOneHotPort inputPlayer, String name, int[][] playerMoves) {
        int failCount = 0;
        int successCount = 0;
        playersNet.clearPortValues();
        for (int[] playerMove : playerMoves) {
            if (playersNet.verify(playerMove,
                    (bfp, cycle) -> {
                        inputPlayer.setItem(name, bfp, cycle);
                    }, (in, out) -> {
                        return checkRules(in, out);
                    })) {
                successCount++;
            } else {
                failCount++;
            }
        }
        if (failCount == 0) {
            LOG.info("Verify-" + nr + " " + name + " OK - Tests succeeded=" + successCount + " failed=" + failCount);
        } else {
            LOG.error("Verify-" + nr + " " + name + " FAILED - Tests succeeded=" + successCount + " failed=" + failCount);
        }
        return failCount;
    }
}

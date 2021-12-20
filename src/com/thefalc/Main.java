package com.thefalc;

import java.util.*;
import java.util.stream.Collectors;

// TODO:
// Write README
// Run simulations and produce results

/**
 * Gift object. The higher the value, the better the gift.
 */
class Gift {
    double value;
    int steals;
    int index;

    public Gift(double value, int index) {
        this.value = value;
        this.index = index;
    }
}

/**
 * Player object. Players have a randomly assigned strategy and are trying to maximize their gift value.
 */
class Player {
    Gift gift;
    Strategy strategy;
    int index;

    public Player(Strategy strategy, int index) {
        this.strategy = strategy;
        this.index = index;
    }
}

public class Main {
    private static List<Strategy> strategies = new ArrayList<>();
    private static List<List<Player>> gamesLog = new ArrayList<>();

    public static void main(String[] args) {
        // Parse input parameters and set defaults
        int totalPlayers = args.length > 1 ? Integer.parseInt(args[0]) : GameUtilities.DEFAULT_NUM_OF_PLAYERS;
        int iterations = args.length > 1 ? Integer.parseInt((args[1])) : GameUtilities.DEFAULT_NUM_OF_ITERATIONS;
        int maxSteals = args.length > 2 ? Integer.parseInt((args[2])) : GameUtilities.DEFAULT_NUM_OF_STEALS;
        boolean letPlayerOneGoAgain = args.length > 3
            ? Boolean.parseBoolean(args[3]) : GameUtilities.DEFAULT_PLAYER_ONE_PLAY_AGAIN;

        strategies.add(new AlwaysOpen());
        strategies.add(new AlwaysSteal());
        strategies.add(new StealOnCoinFlip());
        strategies.add(new StealAboveMean());
        strategies.add(new StealNearlyDeadGift());

        // Run game simulation
        for(int i = 0; i < iterations; i++) {
            List<Gift> gifts = initGifts(totalPlayers);
            List<Player> players = initPlayers(totalPlayers);
            YankeeSwap yankeeSwap = new YankeeSwap(players, gifts, maxSteals, letPlayerOneGoAgain);

            // Randomize order of play
            Collections.shuffle(players);

            while(gifts.size() > 0) {
                yankeeSwap.playGame();
            }

            // Store history of the game
            gamesLog.add(players);
            System.out.println();
        }

        System.out.println();
        System.out.println("POSITIONAL STATS");
        showPositionalStats(gamesLog, totalPlayers);

        System.out.println();
        System.out.println("STRATEGY STATS");
        showStrategyStats(gamesLog);
    }

    /**
     * Goes through all simulated games and computes the mean gift value based on the position the player played.
     */
    private static void showPositionalStats(List<List<Player>> gamesLog, int totalPlayers) {
        double giftValuesByPlayPosition[] = new double[totalPlayers];

        // Go through each game
        for(List<Player> players : gamesLog) {
            // Add gift value based on selection position
            for(Player player : players) {
                giftValuesByPlayPosition[player.index - 1] += player.gift.value;
            }
        }

        for(int i = 0; i < giftValuesByPlayPosition.length; i++) {
            double averageValue = giftValuesByPlayPosition[i] / gamesLog.size();

            System.out.println((i + 1) + ", " + averageValue);
        }
    }

    /**
     * Goes through all simulated games and computes the mean gift value based on the strategy used.
     */
    private static void showStrategyStats(List<List<Player>> gamesLog) {
        double giftValuesByStrategy[] = new double[strategies.size()];
        int totalTimeStrategyWasUsed[] = new int[strategies.size()];

        // Go through each game
        for(List<Player> players : gamesLog) {
            // Add gift value based on selection position
            for(Player player : players) {
                int strategyIndex = getStrategyIndex(player.strategy);
                giftValuesByStrategy[getStrategyIndex(player.strategy)] += player.gift.value;
                totalTimeStrategyWasUsed[strategyIndex]++;
            }
        }

        for(int i = 0; i < giftValuesByStrategy.length; i++) {
            double averageValue = giftValuesByStrategy[i] / totalTimeStrategyWasUsed[i];

            System.out.println(strategies.get(i).getClass().getCanonicalName() + ", " + averageValue);
        }
    }

    private static int getStrategyIndex(Strategy playerStrategy) {
        for(int i = 0; i < strategies.size(); i++) {
            if(strategies.get(i).getClass().getCanonicalName().equals(playerStrategy.getClass().getCanonicalName())) {
                return i;
            }
        }

        return -1;
    }

    private static List<Gift> initGifts(int totalGifts) {
        List<Gift> gifts = new ArrayList<>(totalGifts);
        for(int i = 0; i < totalGifts; i++) {
            Gift gift = new Gift(Math.random(), i + 1);
            gifts.add(gift);
        }

        return gifts;
    }

    private static List<Player> initPlayers(int totalPlayers) {
        List<Player> players = new ArrayList<>(totalPlayers);
        for(int i = 0; i < totalPlayers; i++) {
            int strategyIndex = (int)(Math.random() * strategies.size());
            Player player = new Player(strategies.get(strategyIndex), i + 1);
            players.add(player);
        }

        return players;
    }
}

class GameUtilities {
    public static int DEFAULT_NUM_OF_STEALS = 3;
    public static int DEFAULT_NUM_OF_PLAYERS = 10;
    public static int DEFAULT_NUM_OF_ITERATIONS = 10000;
    public static boolean DEFAULT_PLAYER_ONE_PLAY_AGAIN = false;

    public static void printOpen(Player player, Gift gift) {
        System.out.println("Player " + player.index + " OPENED gift " + gift.index + " with value "
            + gift.value);
    }

    public static void printSteal(Player currentPlayer, Player priorGiftOwner, Gift gift) {
        System.out.println("Player " + currentPlayer.index + " STOLE gift " + gift.index + " from "
            + priorGiftOwner.index + " with value " + gift.value);
    }
}
package com.thefalc;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main class for executing the Yankee Swap simulation.
 */
public class YankeeSwap {
  private List<Player> players;
  private List<Gift> gifts;
  private int maxSteals;
  private boolean letPlayerOneGoAgain;
  private Set<Gift> stolenThisRound;

  public YankeeSwap(List<Player> players, List<Gift> gifts, int maxSteals, boolean letPlayerOneGoAgain) {
    this.setPlayers(players);
    this.setGifts(gifts);
    this.setMaxSteals(maxSteals);
    this.setLetPlayerOneGoAgain(letPlayerOneGoAgain);
    this.setStolenThisRound(new HashSet<>());
  }

  /**
   * Runs game simulation until all gifts have been chosen.
   */
  public void playGame() {
    for(int i = 0; i < getPlayers().size(); i++) {
      Player player = getPlayers().get(i);

      // First player always opens a gift
      if(i == 0) {
        selectRandomGift(player);
      }
      else {
        player.strategy.applyStrategy(player, this);
      }

      // Refresh the stolen this round set
      getStolenThisRound().clear();
    }

    // Check if the first player gets to go again
    if(isLetPlayerOneGoAgain()) {
      getPlayers().get(0).strategy.applyStrategy(getPlayers().get(0), this);
    }
  }

  /**
   * Chooses a random gift from those that are unoopened.
   */
  private Gift getRandomGift(List<Gift> gifts) {
    if(gifts.size() == 0) return null;

    int giftIndex = (int)(Math.random() * gifts.size());

    return gifts.get(giftIndex);
  }

  /**
   * Assigns a random gift to the passed in player. This is equivalent to a participant picking
   * a wrapped gift randomly and opening it.
   */
  public void selectRandomGift(Player player) {
    // Give gift to player and remove from available wrapped gifts
    Gift gift = getRandomGift(gifts);
    if(gift != null) {
      player.gift = getRandomGift(gifts);
      gifts.remove(player.gift);

      GameUtilities.printOpen(player, player.gift);
    }
  }

  /**
   * The current player takes the gift from the player to steam from and the
   * player to steal from ends up with the current player's gift.
   */
  public void stealGift(Player currentPlayer, Player playerToStealFrom) {
    Gift currentGift = currentPlayer.gift;
    currentPlayer.gift = playerToStealFrom.gift;
    currentPlayer.gift.steals++;
    stolenThisRound.add(currentPlayer.gift);
    playerToStealFrom.gift = currentGift;

    GameUtilities.printSteal(currentPlayer, playerToStealFrom, currentPlayer.gift);
  }

  public List<Player> getPlayers() {
    return players;
  }

  public void setPlayers(List<Player> players) {
    this.players = players;
  }

  public List<Gift> getGifts() {
    return gifts;
  }

  public void setGifts(List<Gift> gifts) {
    this.gifts = gifts;
  }

  public int getMaxSteals() {
    return maxSteals;
  }

  public void setMaxSteals(int maxSteals) {
    this.maxSteals = maxSteals;
  }

  public boolean isLetPlayerOneGoAgain() {
    return letPlayerOneGoAgain;
  }

  public void setLetPlayerOneGoAgain(boolean letPlayerOneGoAgain) {
    this.letPlayerOneGoAgain = letPlayerOneGoAgain;
  }

  public Set<Gift> getStolenThisRound() {
    return stolenThisRound;
  }

  public void setStolenThisRound(Set<Gift> stolenThisRound) {
    this.stolenThisRound = stolenThisRound;
  }
}

interface Strategy {
  public void applyStrategy(Player currentPlayer, YankeeSwap game);
}

/**
 * Always open a random gift strategy. If there are not any gifts to open, randomly select any opened gift to steal.
 */
class AlwaysOpen implements Strategy {
  public void applyStrategy(Player currentPlayer, YankeeSwap game) {
    // Player doesn't do anything if they already have gift
    if(currentPlayer.gift == null) {
      // There are unopened gifts, so select one at random
      if(game.getGifts().size() > 0) {
        game.selectRandomGift(currentPlayer);
      }
      else { // Steal random gift
        // Create list of eligible players to steal from
        List<Player> playersWithGifts = game.getPlayers().stream()
            .filter(p -> p.gift.steals < game.getMaxSteals()
                && p.gift != null && p.index != currentPlayer.index
                && !game.getStolenThisRound().contains(p.gift)).collect(Collectors.toList());

        if(playersWithGifts.size() > 0) {
          // Steal gift from random player
          int randomGiftToSteal = new Random().nextInt(playersWithGifts.size());
          Player playerToStealFrom = playersWithGifts.get(randomGiftToSteal);

          game.stealGift(currentPlayer, playerToStealFrom);

          // Apply strategy for player that was stolen from
          playerToStealFrom.strategy.applyStrategy(playerToStealFrom, game);
        }
      }
    }
  }
}

/**
 * Always steal highest value available gift.
 */
class AlwaysSteal implements Strategy {
  public void applyStrategy(Player currentPlayer, YankeeSwap game) {
    Player playerToStealFrom = getBestGift(currentPlayer.index, game);

    // Steal gift if one was found
    if(playerToStealFrom != null) {
      game.stealGift(currentPlayer, playerToStealFrom);

      // Apply strategy for player that was stolen from
      playerToStealFrom.strategy.applyStrategy(playerToStealFrom, game);
    }
    else { // Nothing to steal, have to open a gift
      game.selectRandomGift(currentPlayer);
    }
  }

  private Player getBestGift(int currentPlayerIndex, YankeeSwap game) {
    double highestValueItem = 0;
    Player playerToStealFrom = null;

    // Find the best gift available
    for(Player player : game.getPlayers()) {
      if((shouldSteal(currentPlayerIndex, player, highestValueItem, game))) {
        highestValueItem = player.gift.value;
        playerToStealFrom = player;
      }
    }

    return playerToStealFrom;
  }

  private boolean shouldSteal(int currentPlayerIndex, Player player, double highestValueItem, YankeeSwap game) {
    return player.gift != null && player.gift.steals < game.getMaxSteals()
        && player.gift.value > highestValueItem
        && player.index != currentPlayerIndex
        && !game.getStolenThisRound().contains(player.gift);
  }
}

/**
 * Steal a gift based on the outcome of a coin flip.
 */
class StealOnCoinFlip implements Strategy {
  public void applyStrategy(Player currentPlayer, YankeeSwap game) {
    int coinFlip = new Random().nextInt(2);

    // Steal best available gift if coin flip is zero
    if(coinFlip == 0) {
      new AlwaysSteal().applyStrategy(currentPlayer, game);
    }
    else { // Open random gift
      game.selectRandomGift(currentPlayer);
    }
  }
}

/**
 * Steal a gift if one exists with a value above the mean value of the currently unwrapped gifts.
 */
class StealAboveMean implements Strategy {
  public void applyStrategy(Player currentPlayer, YankeeSwap game) {
    double highestValueItem = 0;
    double mean = 0;
    int totalSeen = 0;

    // Compute the mean value of existing unwrapped gifts
    for(Player player : game.getPlayers()) {
      if(player.gift != null) {
        mean += player.gift.value;
        totalSeen++;

        if(shouldSteal(currentPlayer.index, player, highestValueItem, game)) {
          highestValueItem = player.gift.value;
        }
      }
    }

    mean /= totalSeen;

    // Steal the best available gift if value is higher than the mean
    if(highestValueItem > mean) {
      new AlwaysSteal().applyStrategy(currentPlayer, game);
    }
    else { // Open random gift
      game.selectRandomGift(currentPlayer);
    }
  }

  private boolean shouldSteal(int currentPlayerIndex, Player player, double highestValueItem, YankeeSwap game) {
    return player.gift.value > highestValueItem && player.index != currentPlayerIndex
        && player.gift.steals < game.getMaxSteals() && !game.getStolenThisRound().contains(player.gift);
  }
}

/**
 * Steal a gift if one exists with a value above the mean value of the currently
 * unwrapped gifts and it is about to be no longer be available to steal.
 */
class StealNearlyDeadGift implements Strategy {
  public void applyStrategy(Player currentPlayer, YankeeSwap game) {
    double highestValueItem = 0;
    double mean = 0;
    int totalSeen = 0;
    Player playerToStealFrom = null;

    // Compute the mean value of existing unwrapped gifts
    for(Player player : game.getPlayers()) {
      if(player.gift != null) {
        mean += player.gift.value;
        totalSeen++;

        if(shouldSteal(currentPlayer.index, player, highestValueItem, game)) {
          highestValueItem = player.gift.value;
          playerToStealFrom = player;
        }
      }
    }

    mean /= totalSeen;

    // Steal about to be unstealable gift if the value is above the current mean
    if(highestValueItem > mean) {
      game.stealGift(currentPlayer, playerToStealFrom);

      // Apply strategy for player that was stolen from
      playerToStealFrom.strategy.applyStrategy(playerToStealFrom, game);
    }
    else { // Open random gift
      new AlwaysSteal().applyStrategy(currentPlayer, game);
    }
  }

  private boolean shouldSteal(int currentPlayerIndex, Player player, double highestValueItem, YankeeSwap game) {
    return player.gift.steals == game.getMaxSteals() - 1
        && player.gift.value > highestValueItem
        && player.index != currentPlayerIndex
        && !game.getStolenThisRound().contains(player.gift);
  }
}
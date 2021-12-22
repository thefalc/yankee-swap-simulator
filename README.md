# Yankee Swap (White Elephant/Bad Santa) Simulator

This code sample simulates the Yankee Swap holiday gift exchange game applying various strategies.

Check out this [article](https://thefalc.com/2021/12/the-optimal-white-elephant-strategy/) for a analytical
breakdown of which strategies work best and how your position in the game impacts your ability to receive
a great gift.

## Game assumptions

There are many versions of this game. The version played can have large impact on the strategies that work best.

For the purposes of this simulation, I assumed the following game play.

* Player selection order is random.
* The first player to select must open a gift.
* The second player can choose to steal the first players gift or open a new gift. If they steal, then
the first player must open a new gift since there is nothing to steal except their original gift.
* The third player can choose to steal from players 1 or 2 or open a new gift. If they steal, then
the player they stole from can choose to steal or open a gift. This continues until someone must open
a gift.
* Swap cycles within one round are not allowed.
* An item can only be swapped a maximum number (default is 3) of times until it is no longer eligible for swapping.

There is also an optional rule to allow the first player to play again after everyone has gone.

## Game strategies

This code sample includes 5 different gift picking strategies:

1. Always open a gift if possible otherwise pick a random gift to steal.
1. Always steal the unwrapped gift with the highest value.
1. Flip a coin and if tails, steal the unwrapped gift with the highest value. Otherwise open a random gift.
1. Calculate the mean of the currently available gifts and steal the highest value one if it is above the mean.
1. Steal the highest value item that has been swapped max number of times - 1 (nearly dead) and has a value above
the current mean. Otherwise steal the highest value gift available.

## Prerequisite

You must have the following software installed on your machine:

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

## Execution

### Compile

In a terminal, navigate to the code's project directory.

```bash
javac -d out/ src/com/thefalc/*.java
```

### Run simulation

To run the simulation with default settings, from the current code's project directly execute the following:

Usage:

```bash
java -cp out com.thefalc.Main
```

The default number of players is 10.
The default number of iteractions is 10,000.
The default number of steals is 3.
By default, the first player doesn't get a second chance.

Alternative usage:

```bash
java -cp out com.thefalc.Main NUM_OF_PLAYERS NUM_OF_ITERATIONS NUM_OF_STEALS PLAYER_ONE_PLAYS_AGAIN
```

Where `NUM_OF_PLAYERS` is how many players are participating in the simulation, `NUM_OF_ITERATIONS` is how many
times to run the simulation, `NUM_OF_STEALS` is the maximum times an item can be stolen, and `PLAYER_ONE_PLAYS_AGAIN` is
true|false representing whether the first player gets to play again at the end of the game.

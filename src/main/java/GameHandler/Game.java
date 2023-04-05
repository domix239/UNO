package GameHandler;

import CardManager.Card;
import CardManager.Deck;
import Players.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Game {
    Player[] players;
    Deck deck;
    boolean isOver = false;

    public Game(Player[] players) {
        System.out.println("-- Creating Deck --");
        deck = new Deck();
        deck.shuffle();
        System.out.println("-- Deck created --");
        this.players = players;
        handOutCards();
    }

    private void handOutCards() {
        for (int i = 0; i < 3; i++) {
            for (Player player : players) {
                ArrayList<Card> playerCards = player.getCards() != null ? player.getCards() : new ArrayList<>();
                if (i % 2 == 0)
                    Collections.addAll(playerCards,handOutNumberOfCards(2));
                else
                    Collections.addAll(playerCards, handOutNumberOfCards(3));
                player.setCards(playerCards);
            }
        }
    }

    private Card[] handOutNumberOfCards(int number) {
        Card[] drawHand = new Card[number];
        for (int i = 0; i < number; i++) {
            drawHand[i] = deck.draw();
        }
        return drawHand;
    }

    public void start() {
        do {
            for (Player player :
                    players) {
                if (player.getId() == 0){
                    // do your logic here
                } else {
                    // implement AI logic here
                }
            }
        } while (!isOver);
    }
}

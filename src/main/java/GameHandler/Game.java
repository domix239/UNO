package GameHandler;

import CardManager.Card;
import CardManager.Deck;
import Players.Player;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class Game {
    final String[] COLORS = new String[]{"green", "yellow", "blue", "red"};
    Player[] players;
    Deck deck;
    boolean isOver = false;
    Card upperCard;

    public Game(Player[] players) throws InterruptedException {
        System.out.println("-- Creating Deck --");
        deck = new Deck();
        deck.shuffle();
        System.out.println("-- Deck created --");
        this.players = players;
        handOutCards();
        // Discard Deck
        if (verifyFirstCard(deck.getCards().get(0))) {
            upperCard = deck.draw();
        } else {
            do {
                System.out.println("Invalid upper card -> Reshuffling");
                deck.shuffle();
                Thread.sleep(500);
            } while (!verifyFirstCard(deck.getCards().get(0)));
            upperCard = deck.draw();
        }
    }

    private boolean verifyFirstCard(Card card) {
        return !card.getColor().equalsIgnoreCase("wild") && card.getNumberOrAction() instanceof Integer;
    }

    private void handOutCards() {
        for (int i = 0; i < 3; i++) {
            for (Player player : players) {
                ArrayList<Card> playerCards = player.getCards() != null ? player.getCards() : new ArrayList<>();
                if (i % 2 == 0)
                    Collections.addAll(playerCards, handOutNumberOfCards(2));
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

    public void start() throws InterruptedException {
        boolean isRight = true;
        do {
            for (Player player :
                    players) {
                System.out.println("Player " + player.getName() + "s turn.");
                System.out.println("\nDiscard Deck: [" + upperCard.getColor().toUpperCase() + " " + upperCard.getNumberOrAction() + "]");
                if (checkAndDrawCards(player))
                    continue;
                if (checkAndSkip()) {
                    continue;
                }
                if (checkAndReverse()) {
                    int rotationDistance = player.getId() == 0 ? players.length - 1 : -1;
                    Collections.rotate(Arrays.asList(players), rotationDistance);
                    continue;
                }
                if (upperCard.getNumberOrAction().equals("+4")) {
                    Collections.addAll(player.getCards(), handOutNumberOfCards(4));
                }

                if (player.getId() == 0) {
                    System.out.println("Choose a card to play: \n");
                    ArrayList<Card> playerCards = player.getCards();
                    int counter = -1;
                    for (Card c :
                            playerCards) {
                        counter++;
                        System.out.println("(" + counter + ") [" + c.getColor().toUpperCase() + " " + c.getNumberOrAction() + "]");
                    }

                    Scanner scanner = new Scanner(System.in);
                    boolean validInput = false;
                    int discardCardId = getAndVerifyUserInput(scanner, counter);
                    Card discardCard = playerCards.remove(discardCardId);
                    if (discardCard.getColor().equalsIgnoreCase("wild")) {
                        System.out.println("What color do you request? ");
                        int index = -1;
                        for (String color :
                                COLORS) {
                            index++;
                            System.out.println("(" + index + ") " + color.toUpperCase());
                        }
                        String chosenColor = COLORS[getAndVerifyUserInput(scanner, index)];
                        discardCard.setColor(chosenColor);
                    }
                } else {
                    Thread.sleep(500);
                    // implement AI logic here
                    // Card card = player.decideDrop(upperCard);
                    if (player.getId() == 3)
                        upperCard = new Card("wild", "reverse");
                }
            }
        } while (!isOver);
    }

    private int getAndVerifyUserInput(Scanner scanner, int upperBoundary) {
        int index = 0;
        boolean validInput = false;
        do {
            try {
                index = Integer.parseInt(scanner.nextLine());
                if ((index < 0) || index > upperBoundary) {
                    throw new IndexOutOfBoundsException("The ID must lay between 0 and " + upperBoundary + ".");
                } else
                    validInput = true;
            } catch (NumberFormatException ex) {
                System.out.println("Wrong input type - must be a number.");
            } catch (IndexOutOfBoundsException ex) {
                System.out.println(ex.getMessage());
            }
        } while (!validInput);
        return index;
    }

    private boolean checkAndDrawCards(Player player) {
        if (upperCard.getNumberOrAction().equals("+4")) {
            addToPlayerHands(player,4);
            return true;
        }
        if (upperCard.getNumberOrAction().equals("+2")) {
            addToPlayerHands(player,2);
            return true;
        }
        return false;
    }

    private boolean checkAndSkip() {
        return upperCard.getNumberOrAction().equals("skipped");
    }

    private boolean checkAndReverse() {
        return upperCard.getNumberOrAction().equals("reverse");
    }

    private void addToPlayerHands(Player player, int numberOfCards) {
        Collections.addAll(player.getCards(),handOutNumberOfCards(numberOfCards));
    }

    private void changePlayerOrder(int currentPlayer) {
        Player[] reversed = new Player[players.length];
        for (int i = 0; i < players.length; i++) {
            reversed[(i+1) % players.length] = players[i];
        }
        players = reversed;
    }

}

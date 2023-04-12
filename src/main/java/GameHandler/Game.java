package GameHandler;

import CardEnums.Actions;
import CardEnums.Colors;
import CardManager.Card;
import CardManager.Deck;
import Players.Player;
import lombok.Value;

import java.util.*;

public class Game {
    final String[] COLORS = new String[]{Colors.RED, Colors.BLUE, Colors.RED, Colors.GREEN};
    Player[] players;
    Deck deck;
    boolean isOver = false;
    Card upperCard;

    Integer AI_TIMEOUT;

    public Game(Player[] players) throws InterruptedException {

        AI_TIMEOUT = Integer.parseInt(System.getProperty("ai.timeout","2500"));

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
        return !card.getColor().equalsIgnoreCase(Colors.WILD) && card.getNumberOrAction() instanceof Integer;
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
        int round = 0;
        do {
            int playerIndex = -1;
            for (Player player :
                    players) {
                ArrayList<Card> playerCards = player.getCards();
                round++;
                if (checkAndDrawCards(player) || checkAndSkip()) {
                    System.out.println("Player " + player.getName() + "s turn was skipped!");
                    continue;
                }
                System.out.println("\n\nPlayer " + player.getName() + "s turn.");
                System.out.println("Discard Pile: [" + upperCard.getColor().toUpperCase() + " " + upperCard.getNumberOrAction() + "]");

                if (player.getId() == 0) {
                    System.out.println("Choose a card to play: \n");

                    int counter = logCardsToConsole(playerCards);
                    counter++;
                    System.out.println("(" + counter + ") Draw a card");
                    Scanner scanner = new Scanner(System.in);

                    int discardCardId = getAndVerifyUserSelectedCard(playerCards, scanner, counter);
                    if (discardCardId == -1) {
                        addToPlayerHands(player, 1);
                        counter = logCardsToConsole(playerCards);
                        discardCardId = getAndVerifyUserSelectedCard(playerCards, scanner, counter);
                    }

                    Card discardCard = playerCards.remove(discardCardId);
                    if (discardCard.getColor().equalsIgnoreCase(Colors.WILD)) {
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
                    upperCard = discardCard;
                } else {
                    Thread.sleep(AI_TIMEOUT);

                    Card decided = doDecision(player, playerIndex);

                    if (decided == null) {
                        System.out.println("Player " + player.getName() + "draws a card.");
                        addToPlayerHands(player, 1);
                        decided = doDecision(player, playerIndex);
                    }
                    upperCard = decided != null ? decided : upperCard;
                    if (decided == null)
                        continue;
                }

                // implement uno checker

                // implement win checker
                isOver = winChecker(playerCards.size());

                // reverse the player array and re-start the game loop
                if (checkAndReverse()) {
                    Collections.reverse(Arrays.asList(players));
                    int rotationDistance = player.getId() == 0 ? players.length - 1 : -1;
                    Collections.rotate(Arrays.asList(players), rotationDistance);
                    start();
                }
            }
        } while (!isOver);
    }

    private boolean winChecker(int playerCards) {
        return playerCards == 0;
    }

    private int getAndVerifyUserSelectedCard(ArrayList<Card> playerCards, Scanner scanner, int upperBoundary) {
        boolean validInput = false;
        int discardCardId;
        do {
            discardCardId = getAndVerifyUserInput(scanner, upperBoundary);
            if (playerCards.size() == discardCardId) return -1;
            if ((playerCards.get(discardCardId).getColor().equals(upperCard.getColor())) ||
                    (playerCards.get(discardCardId).getNumberOrAction().equals(upperCard.getNumberOrAction()))) {
                validInput = true;
            } else {
                System.out.println("Prohibited card. Color or Number did not match. Choose another card or draw a card.");
            }
        } while (!validInput);
        return discardCardId;
    }

    private int logCardsToConsole(ArrayList<Card> playerCards) {
        int counter = -1;
        for (Card c :
                playerCards) {
            counter++;
            System.out.println("(" + counter + ") [" + c.getColor().toUpperCase() + " " + c.getNumberOrAction() + "]");
        }
        return counter;
    }

    private Card doDecision(Player player, int playerIndex) {
        return player.decideDrop(upperCard, playerIndex + 1 == players.length ?
                players[0].getCards().size() :
                players[playerIndex + 1].getCards().size());
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
        if (upperCard.getNumberOrAction().equals(Actions.DRAW_FOUR)) {
            addToPlayerHands(player, 4);
            upperCard.setNumberOrAction("_" + Actions.DRAW_FOUR + "_");
            return true;
        }
        if (upperCard.getNumberOrAction().equals("+2")) {
            addToPlayerHands(player, 2);
            upperCard.setNumberOrAction("_" + Actions.DRAW_TWO + "_");
            return true;
        }
        return false;
    }

    private boolean checkAndSkip() {
        boolean equals = upperCard.getNumberOrAction().equals(Actions.SKIPPED);
        if (equals)
            upperCard.setNumberOrAction("_" + Actions.SKIPPED + "_");
        return equals;
    }

    private boolean checkAndReverse() {
        return upperCard.getNumberOrAction().equals(Actions.REVERSE);
    }

    private void addToPlayerHands(Player player, int numberOfCards) {
        System.out.println("Player " + player.getName() + " draws " + numberOfCards + " card(s).");
        Collections.addAll(player.getCards(), handOutNumberOfCards(numberOfCards));
    }
}

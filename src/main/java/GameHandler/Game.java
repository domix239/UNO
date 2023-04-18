package GameHandler;

import CardEnums.Actions;
import CardEnums.Colors;
import CardManager.Card;
import CardManager.Deck;
import Players.Player;
import lombok.Value;

import java.net.CookieHandler;
import java.util.*;

public class Game {
    final String[] COLORS = new String[]{Colors.RED, Colors.BLUE, Colors.RED, Colors.GREEN};
    Player[] players;
    Deck deck;
    boolean isOver = false;
    Card upperCard;
    ArrayList<Card> discardPile = new ArrayList<>();

    Integer AI_TIMEOUT;

    boolean calledUno = false;

    final private float CHANCE_TO_MISS_UNO = 0.05f;

    boolean isReversed = false;

    public Game(Player[] players) throws InterruptedException {

        AI_TIMEOUT = Integer.parseInt(System.getProperty("ai.timeout", "2500"));

        System.out.println("-- Creating Deck --");
        deck = new Deck();
        deck.shuffle();
        System.out.println("-- Deck created --");
        this.players = players;
        handOutCards();
        // Discard Deck
        if (verifyFirstCard(deck.getCards().get(0))) {
            // TODO replace
            // upperCard = deck.draw();
            discardPile.add(deck.draw());
        } else {
            do {
                System.out.println("Invalid upper card -> Reshuffling");
                deck.shuffle();
                Thread.sleep(500);
            } while (!verifyFirstCard(deck.getCards().get(0)));
            // TODO replace
            // upperCard = deck.draw();
            discardPile.add(deck.draw());
        }
    }

    private Card getUpperCard() {
        return discardPile.get(discardPile.size() - 1);
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

            if (deck.getCards().size() >= 1) {
                drawHand[i] = deck.draw();
            } else {
                System.out.println("UNO Deck depleted. Reshuffling Discard Pile ...");
                shuffleDiscardPile();
            }
        }
        return drawHand;
    }

    private void shuffleDiscardPile() {
        Card upperCard = discardPile.remove(discardPile.size() - 1);
        Collections.shuffle(discardPile);
        deck.setCards(discardPile);
        discardPile = new ArrayList<>();
        discardPile.add(upperCard);
    }

    public void start() throws InterruptedException {
        int round = 0;
        do {
            int playerIndex = -1;
            for (Player player :
                    players) {

                calledUno = false;

                System.out.println("DECK SIZE: "+deck.getCards().size()+" || DISCARD PILE SIZE: "+discardPile.size());

                ArrayList<Card> playerCards = player.getCards();
                round++;
                if (checkAndDrawCards(player) || checkAndSkip()) {
                    System.out.println("Player " + player.getName() + "s turn was skipped!");
                    continue;
                }
                System.out.println("\n\nPlayer " + player.getName() + "s turn. ("+playerCards.size()+" cards in hand)");
                System.out.println("Discard Pile: [" + getUpperCard().getColor().toUpperCase() + " " + getUpperCard().getNumberOrAction() + "]");

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
                        counter++;
                        System.out.println("(" + counter + ") Skip turn");
                        discardCardId = getAndVerifyUserSelectedCard(playerCards, scanner, counter);

                        if (discardCardId == -1) {
                            continue;
                        }
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
                    // TODO replace
                    // upperCard = discardCard;
                    discardPile.add(discardCard);
                } else {
                    Thread.sleep(AI_TIMEOUT);

                    Card decided = doDecision(player, playerIndex);

                    if (decided == null) {
                        addToPlayerHands(player, 1);
                        decided = doDecision(player, playerIndex);
                    }
                    if (decided != null)
                        discardPile.add(decided);
                    if (decided == null)
                        continue;

                    if (playerCards.size() == 1 && CHANCE_TO_MISS_UNO < new Random().nextFloat()) {
                        System.out.println("#####################################");
                        System.out.println("Player "+player.getName()+" says: UNO!");
                        System.out.println("#####################################");
                        calledUno = true;
                    }
                }

                // implement uno checker

                if (!calledUno && playerCards.size() == 1) {
                    System.out.println("Player "+player.getName()+" did not say UNO - draw 2 cards!");
                    addToPlayerHands(player,2);
                }

                // implement win checker
                isOver = winChecker(playerCards.size());
                if (isOver) {
                    System.out.println("#####################################");
                    System.out.println("Player " + player.getName() + " wins!");
                    System.out.println("#####################################");
                    // calculate points
                    System.exit(0);
                }
                // reverse the player array and re-start the game loop
                if (checkAndReverse()) {
                    isReversed = changeGameDirection(isReversed);

                    // counterclockwise
/*                    if (isReversed) {
                        for (int i = 0; i < players.length - 1; i++) {
                            for (int j = 0; j < players.length - i - 1; j++) {
                                if (players[j].getId() < players[j + 1].getId()) {
                                    Player temp = players[j];
                                    players[j] = players[j + 1];
                                    players[j + 1] = temp;
                                }
                            }
                        }
                    }
                    // clockwise
                    else {
                        for (int i = 0; i < players.length - 1; i++) {
                            for (int j = 0; j < players.length - i - 1; j++) {
                                if (players[j].getId() > players[j + 1].getId()) {
                                    Player temp = players[j];
                                    players[j] = players[j + 1];
                                    players[j + 1] = temp;
                                }
                            }
                        }
                    }

                    if (player.getId() == 0)
                        Collections.rotate(Arrays.asList(players),players.length);
                    getUpperCard().setNumberOrAction("_"+Actions.REVERSE+"_");*/


                    Collections.reverse(Arrays.asList(players));
                    int rotationDistance = isReversed ? 1 : -1;
                    Collections.rotate(Arrays.asList(players), rotationDistance);
                    getUpperCard().setNumberOrAction("_"+Actions.REVERSE+"_");
                    start();

                    break;
                }
            }
        } while (!isOver);
    }

    private boolean changeGameDirection(boolean isReversed) {
        System.out.println("##############################################################");
        System.out.println("Changed game direction: "+(isReversed ? "(CCW)" : "(CW)")+" ==> "+(!isReversed ? "(CCW)" : "(CW)"));
        System.out.println("##############################################################");
        return !isReversed;
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
            if ((playerCards.get(discardCardId).getColor().equals(getUpperCard().getColor())) ||
                    (playerCards.get(discardCardId).getNumberOrAction().equals(getUpperCard().getNumberOrAction())) ||
                    (playerCards.get(discardCardId).getColor().equals(Colors.WILD))) {
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
        return player.decideDrop(getUpperCard(), playerIndex + 1 == players.length ?
                players[0].getCards().size() :
                players[playerIndex + 1].getCards().size());
    }

    private int getAndVerifyUserInput(Scanner scanner, int upperBoundary) {
        int index = 0;
        boolean validInput = false;
        do {
            try {
                String usrInput = scanner.nextLine().toUpperCase();
                if (usrInput.contains("UNO")) {
                    calledUno = true;
                    usrInput = usrInput.replace(" ","");
                    usrInput = usrInput.replace("UNO","");
                }
                index = Integer.parseInt(usrInput);
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
        if (getUpperCard().getNumberOrAction().equals(Actions.DRAW_FOUR)) {
            addToPlayerHands(player, 4);
            getUpperCard().setNumberOrAction("_" + Actions.DRAW_FOUR + "_");
            return true;
        }
        if (getUpperCard().getNumberOrAction().equals("+2")) {
            addToPlayerHands(player, 2);
            getUpperCard().setNumberOrAction("_" + Actions.DRAW_TWO + "_");
            return true;
        }
        return false;
    }

    private boolean checkAndSkip() {
        boolean equals = getUpperCard().getNumberOrAction().equals(Actions.SKIPPED);
        if (equals)
            getUpperCard().setNumberOrAction("_" + Actions.SKIPPED + "_");
        return equals;
    }

    private boolean checkAndReverse() {
        return getUpperCard().getNumberOrAction().equals(Actions.REVERSE);
    }

    private void addToPlayerHands(Player player, int numberOfCards) {
        System.out.println("Player " + player.getName() + " draws " + numberOfCards + " card(s).");
        Collections.addAll(player.getCards(), handOutNumberOfCards(numberOfCards));
    }
}

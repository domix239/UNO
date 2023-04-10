package Players;

import CardEnums.Actions;
import CardEnums.Colors;
import CardManager.Card;
import Util.CardLogger;
import com.github.javafaker.Faker;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Random;

@Data
public class Player {

    int id;
    @Getter
    String name;

    final double CHANCE_FAIL_PLAY_RANDOM_CARD = 0.1f;
    final double CHANCE_PLAY_SAME_COLOR_HIGHEST_CARD = 1.0f;
    final double CHANCE_PLAY_SAME_COLOR_ACTION = 0.3f;
    final double CHANCE_PLAY_WILD_COLOR_CHANGE = 0.25f;
    final double CHANCE_PLAY_WILD_DRAW_FOUR = 0.25f;
    Random rnd = new Random();

    ArrayList<Card> cards;

    public Player(int id, String name) {
        this.name = name;
    }

    public Player(int id) {
        this.id = id;
        this.name = new Faker().name().firstName();
    }

    /*public Card decideDrop(Card upperCard, int cardAmountNextPlayer) {
        Random rnd = new Random();
        ArrayList<Card> playable = new ArrayList<>();
        ArrayList<Card> sameColorCards = new ArrayList<>();
        ArrayList<Card> sameNumberOrActionCards = new ArrayList<>();
        ArrayList<Card> wildCards = new ArrayList<>();
        AtomicInteger yellowCount = new AtomicInteger();
        AtomicInteger greenCount = new AtomicInteger();
        AtomicInteger blueCount = new AtomicInteger();
        AtomicInteger redCount = new AtomicInteger();

        cards.forEach((card -> {
            switch (card.getColor()) {
                case Colors.GREEN:
                    greenCount.getAndIncrement();
                    break;
                case Colors.RED:
                    redCount.getAndIncrement();
                    break;
                case Colors.BLUE:
                    blueCount.getAndIncrement();
                    break;
                case Colors.YELLOW:
                    yellowCount.getAndIncrement();
                    break;
            }

            if (upperCard.getColor().equals(card.getColor())) {
                sameColorCards.add(card);
            } else if (!upperCard.getColor().equals(card.getColor()) &&
                    upperCard.getNumberOrAction().equals(card.getNumberOrAction())){
                sameNumberOrActionCards.add(card);
            }else if (card.getColor().equals(Colors.WILD)) {
                wildCards.add(card);
            }
        }));

        playable.addAll(sameColorCards);
        playable.addAll(sameNumberOrActionCards);
        playable.addAll(wildCards);
        // check upper card
        // check has same color => if not => check wild // draw
        // check highest number
        // drop card // insert small chance to drop a wild in between

        ArrayList<Card> possiblePlay = new ArrayList<>();

        if (sameColorCards.size() > 0) {
            int hightestNumber = -1;
            int counter = 0;
            int index = 0;
            for (Card card : sameColorCards) {
                Object obj = card.getNumberOrAction();
                if (obj instanceof Integer) {
                    if ((Integer) obj > hightestNumber) {
                        hightestNumber = (Integer) obj;
                        index = counter;
                    }
                }
                counter++;
            }
            // tend to play the highest card possible with 90% possibility
            if (rnd.nextDouble() > CHANCE_FAIL)
                possiblePlay.add(sameColorCards.get(index));
            else possiblePlay.add(sameColorCards.get(new Random().nextInt(sameColorCards.size())));
        }
        if (sameNumberOrActionCards.size() > 0) {

        }

        Card card = cards.get(0);

        System.out.println(name+" played "+ CardLogger.log(card));

        // wild colorpick

        return card;
    }*/

    public Card decideDrop(Card upperCard, int cardsOfNextPlayer) {
        // TODO remove mockdata
    //    upperCard = new Card(Colors.RED, 2);
    //    cards = new ArrayList<>();
    //    /*cards.add(new Card(Colors.GREEN, 0));
    //    cards.add(new Card(Colors.YELLOW, 1));
    //    cards.add(new Card(Colors.GREEN, 3));
    //    cards.add(new Card(Colors.BLUE, 5));
    //    cards.add(new Card(Colors.GREEN, 9));
    //    cards.add(new Card(Colors.RED, Actions.DRAW_TWO));
    //    cards.add(new Card(Colors.YELLOW, 1));*/


    //    cards.add(new Card(Colors.RED, 0));
    //    cards.add(new Card(Colors.RED, 1));
    //    cards.add(new Card(Colors.GREEN, 3));
    //    cards.add(new Card(Colors.RED, 5));
    //    cards.add(new Card(Colors.GREEN, 9));
    //    cards.add(new Card(Colors.RED, Actions.DRAW_TWO));
    //    cards.add(new Card(Colors.YELLOW, 1));

        Card chosenCard = null;
        int highestNumber = -1;

        if (rnd.nextFloat() < CHANCE_PLAY_SAME_COLOR_HIGHEST_CARD) {
            chosenCard = getCardOfSameColor(upperCard);
        }
        if (chosenCard == null) {
            chosenCard = checkSameOrWild(upperCard);
            if (chosenCard == null)
                return null; // draw card
        }
        if (rnd.nextFloat() < CHANCE_FAIL_PLAY_RANDOM_CARD) {
            Card c = checkSameOrWild(upperCard);
            if (c != null) chosenCard = c;
        }

        cards.remove(chosenCard);
        System.out.println("Player "+name+" played "+ CardLogger.log(chosenCard)+".");
        return chosenCard;
    }

    private Card checkSameOrWild(Card upperCard) {
        Card chosenCard = null;
        Card wild = getWildCard();
        Card same = getCardWithSameSymbol(upperCard);
        if (wild != null) {
            if (same != null) {
                chosenCard = rnd.nextBoolean() ? wild : same;
            } else {
                chosenCard = wild;
            }
        } else if (same != null){
            chosenCard = same;
        }
        return chosenCard;
    }

    private Card getCardOfSameColor(Card upperCard) {
        Card chosenCard = null;
        int highestNumber = -1;
        int counter = 0;
        int marker = 0;
        for (Card card : cards) {
            if (card.getColor().equals(upperCard.getColor()) && !upperCard.getColor().equals(Colors.WILD)) {
                if (card.getNumberOrAction() instanceof Integer) {
                    if ((int) card.getNumberOrAction() > highestNumber) {
                        highestNumber = (int) card.getNumberOrAction();
                        chosenCard = card;
                    }
                } else {
                    if (rnd.nextFloat() < CHANCE_PLAY_SAME_COLOR_ACTION) {
                        chosenCard = card;
                    }
                    marker = counter;
                }
            }
            counter++;
        }

        if (chosenCard == null && marker > 0)
            chosenCard = cards.get(marker);

        return chosenCard;
    }

    private Card getCardWithSameSymbol(Card upperCard) {
        Card chosenCard = null;

        ArrayList<Card> playable = new ArrayList<>();
        for (Card card : cards) {
            if (card.getNumberOrAction().equals(upperCard.getNumberOrAction())) {
                playable.add(card);
            }
        }
        if (playable.size() > 0)
            chosenCard = playable.get(rnd.nextInt(playable.size()));

        return chosenCard;
    }


    private Card getWildCard() {
        Card chosenCard = null;

        ArrayList<Card> wildCards = new ArrayList<>();
        for (Card card : cards) {
            if (card.getColor().equals(Colors.WILD)) {
                wildCards.add(card);
            }
        }
        if (wildCards.size() > 0)
            chosenCard = wildCards.get(rnd.nextInt(wildCards.size()));

        return chosenCard;
    }

    private boolean checkChance(int percentChance) {
        if (percentChance < 1 || percentChance > 100) {
            throw new IllegalArgumentException("percentChance must be between 1 and 90");
        }
        Random rand = new Random();
        int num = rand.nextInt(100);
        return num < percentChance;
    }
}

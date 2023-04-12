package Players;

import CardEnums.Actions;
import CardEnums.Colors;
import CardManager.Card;
import Util.CardLogger;
import com.github.javafaker.Color;
import com.github.javafaker.Faker;
import lombok.Data;
import lombok.Getter;

import java.util.*;

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
    final double CHANCE_PLAY_SECOND_COMMON_COLOR = 0.3f;
    final double CHANCE_PLAY_THIRD_COMMON_COLOR = 0.2f;
    final double CHANCE_PLAY_FOURTH_COMMON_COLOR = 0.1f;
    Random rnd = new Random();

    ArrayList<Card> cards;

    public Player(int id, String name) {
        this.name = name;
    }

    public Player(int id) {
        this.id = id;
        this.name = new Faker().name().firstName();
    }

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
        if (chosenCard.getColor().equals(Colors.WILD)) {
            String demandColor = decideColor();
            System.out.println("Player "+name+" demands the color "+demandColor+"!");
            System.out.println("Player "+name+" played "+ CardLogger.logNewColor(chosenCard,demandColor)+".\n");
            chosenCard.setColor(demandColor);
        } else
            System.out.println("Player "+name+" played "+ CardLogger.log(chosenCard)+".\n");
        cards.remove(chosenCard);
        return chosenCard;
    }

    private String decideColor() {
        Map<String, Integer> colorCounts = new HashMap<>();

        Collections.addAll(cards,new Card(Colors.WILD,Actions.DRAW_FOUR));

        for (Card card : cards) {
            String color = card.getColor();
            if (!color.equals(Colors.WILD))
                colorCounts.put(color, colorCounts.getOrDefault(color, 0) + 1);
        }

        List<Map.Entry<String, Integer>> colorCountList = new ArrayList<>(colorCounts.entrySet());
        colorCountList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        String mostCommon = colorCountList.get(0).getKey();
        String secondMostCommon = "";
        String thirdMostCommon = "";
        String fourthMostCommon = "";

        try {
            secondMostCommon = colorCountList.get(1).getKey();
            thirdMostCommon = colorCountList.get(2).getKey();
            fourthMostCommon = colorCountList.get(3).getKey();
        } catch (Exception ignored) {}

        // default: wish for a color where player has the most cards from
        String chosenColor = mostCommon;
        if (!secondMostCommon.equals("") && rnd.nextFloat() < CHANCE_PLAY_SECOND_COMMON_COLOR)
            chosenColor = secondMostCommon;
        if (!thirdMostCommon.equals("") && rnd.nextFloat() < CHANCE_PLAY_THIRD_COMMON_COLOR)
            chosenColor = thirdMostCommon;
        if (!fourthMostCommon.equals("") && rnd.nextFloat() < CHANCE_PLAY_FOURTH_COMMON_COLOR)
            chosenColor = fourthMostCommon;

        return chosenColor;
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

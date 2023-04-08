package CardManager;

import CardEnums.Actions;
import CardEnums.Colors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Data
public class Deck {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    final String[] COLORS = new String[]{Colors.RED, Colors.BLUE, Colors.RED, Colors.GREEN};
    private ArrayList<Card> cards;

    public Deck() {
        cards = new ArrayList<>();

        for (String color :
                COLORS) {
            for (int i = 0; i <= 9; i++) {
                cards.add(new Card(color, i));
                if (i != 0)
                    cards.add(new Card(color, i));
            }
            for (int i = 0; i < 2; i++) {
                cards.add(new Card(color, Actions.DRAW_TWO));
                cards.add(new Card(color, Actions.REVERSE));
                cards.add(new Card(color, Actions.SKIPPED));
            }
        }
        for (int i = 0; i < 4; i++) {
            cards.add(new Card(Colors.WILD, Actions.WILD));
            cards.add(new Card(Colors.WILD, Actions.DRAW_FOUR));
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        return cards.remove(0);
    }
}

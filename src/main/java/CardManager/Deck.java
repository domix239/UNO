package CardManager;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Data
public class Deck {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    final String[] COLORS = new String[]{"red", "yellow", "blue", "green"};
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
                cards.add(new Card(color,"+2"));
                cards.add(new Card(color,"reverse"));
                cards.add(new Card(color,"skipped"));
            }
        }
        for (int i = 0; i < 4; i++) {
            cards.add(new Card("wild","wild"));
            cards.add(new Card("wild","+4"));
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        return cards.remove(0);
    }
}

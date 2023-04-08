package Players;

import CardEnums.Colors;
import CardManager.Card;
import Util.CardLogger;
import com.github.javafaker.Faker;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;

@Data
public class Player {

    int id;
    @Getter
    String name;

    ArrayList<Card> cards;

    public Player(int id, String name) {
        this.name = name;
    }

    public Player(int id) {
        this.id = id;
        this.name = new Faker().name().firstName();
    }

    public Card decideDrop(Card upperCard) {
        String color = upperCard.getColor();
        ArrayList<Card> playable = new ArrayList<>();
        cards.forEach((card -> {
            if (upperCard.getColor().equals(card.getColor())){
                playable.add(card);
            } else if (card.getColor().equals(Colors.WILD))
                playable.add(card);
        }));

        Card card = cards.get(0);

        System.out.println(name+" played "+ CardLogger.log(card));

        // wild colorpick

        return card;
    }
}

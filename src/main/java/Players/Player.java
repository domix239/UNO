package Players;

import CardManager.Card;
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
}

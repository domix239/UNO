package CardManager;

import lombok.Data;

@Data
public class Card {

    Object numberOrAction;
    String color;
    int number;
    String action;

    boolean wasExecuted = false;

    public Card(String color, Object numberOrAction) {
        this.color = color;
        this.numberOrAction = numberOrAction;
        if (numberOrAction instanceof Integer){
            number = (int) numberOrAction;
        } else {
            action = (String) numberOrAction;
        }
    }
}

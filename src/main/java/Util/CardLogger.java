package Util;

import CardManager.Card;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CardLogger {
    public String log(Card card) {
        return "["+card.getColor().toUpperCase()+" "+card.getNumberOrAction()+"]";
    }

    public String logNewColor(Card card, String newColor) {
        return "["+card.getColor().toUpperCase()+" -> "+newColor+" "+card.getNumberOrAction()+"]";
    }
}

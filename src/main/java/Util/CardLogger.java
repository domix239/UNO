package Util;

import CardManager.Card;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CardLogger {
    public String log(Card card) {
        return "["+card.getColor().toUpperCase()+" "+card.getNumberOrAction()+"]";
    }
}

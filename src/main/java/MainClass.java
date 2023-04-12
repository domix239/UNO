import CardManager.Card;
import CardManager.Deck;
import GameHandler.Game;
import Players.Player;

import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);
        System.out.println("What is your name? ");
        String name = in.nextLine();
        System.out.println("How many AI players should join the game? ");
        String input = in.nextLine();

        int amountPlayers = 0;
        try {
            amountPlayers = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input - must be a number!");
            System.exit(0);
        }

        Player[] players = new Player[amountPlayers+1];
        players[0] = new Player(0,name);
        for (int i = 1; i <= amountPlayers; i++) {
            players[i] = new Player(i);
        }

        Game game = new Game(players);
        game.start();

        boolean stop = true;
    }
}

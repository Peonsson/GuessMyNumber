package Server;

import java.util.Random;

/**
 * Created by johanpettersson on 07/09/15.
 */
public class GuessMyNumber {

    int answer;

    public GuessMyNumber(){
        Random rand = new Random();
        answer = rand.nextInt(100) + 1;
        System.out.println("Correct answer: " + Integer.toString(answer));
    }

    public String compare(int guess) {
        if (guess < answer)
            return "LOW";
        else if (guess > answer)
            return "HIGH";
        else
            return "CORRECT";
    }
}

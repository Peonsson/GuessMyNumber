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
    }

    public int compare(int guess) {
        if(guess < answer)
            return -1;
        else if(guess > answer)
            return 1;
        else return 0;
    }
}

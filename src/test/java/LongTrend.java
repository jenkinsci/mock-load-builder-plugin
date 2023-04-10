import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Stephen Connolly
 */
public class LongTrend {
    public static void main(String... args) {
        double sum = 0;
        int count = 0;
        Random entropy = new Random();
        outer:
        while (count < Integer.MAX_VALUE / 4) {
            long nextReport = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
            while (System.currentTimeMillis() < nextReport) {
                double value = Math.exp(entropy.nextGaussian());
                if (sum > 0 && Math.log10(sum) - Math.log10(value) > 15) {
                    System.out.println("underflow");
                    break outer;
                }
                sum = sum + value;
                count++;
            }
            System.out.println("Average = " + (sum / count) + ", n = " + count);
        }
        System.out.println("Average = " + (sum / count) + ", n = " + count);
    }
}

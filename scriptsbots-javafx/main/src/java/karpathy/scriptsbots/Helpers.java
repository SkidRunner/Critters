package karpathy.scriptsbots;

public class Helpers {

    private static boolean deviateAvailable = false;    // flag
    private static double storedDeviate;                // deviate from previous calculation

    public static float randf(float a, float b) {
        return a + (float) (Math.random() * (b - a));
    }

    // uniform random int in [a,b)
    public static int randi(int a, int b) {
        return a + (int) (Math.random() * (b - a));
    }

    // normalvariate random N(mu, sigma)
    public static double randn(double mu, double sigma) {
        double polar, rsquared, var1, var2;
        if (!deviateAvailable) {
            do {
                var1 = 2.0 * (Math.random()) - 1.0;
                var2 = 2.0 * (Math.random()) - 1.0;
                rsquared = var1 * var1 + var2 * var2;
            } while (rsquared >= 1.0 || rsquared == 0.0);
            polar = Math.sqrt(-2.0 * Math.log(rsquared) / rsquared);
            storedDeviate = var1 * polar;
            deviateAvailable = true;
            return var2 * polar * sigma + mu;
        } else {
            deviateAvailable = false;
            return storedDeviate * sigma + mu;
        }
    }

    // cap value between 0 and 1
    public static float cap(float a) {
        if (a < 0) return 0;
        if (a > 1) return 1;
        return a;
    }
}

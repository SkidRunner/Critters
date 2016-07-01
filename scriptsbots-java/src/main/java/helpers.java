public class helpers {

    public static final int RAND_MAX = Integer.MAX_VALUE;

    public static int rand() {
        return (int) (Math.random() * RAND_MAX);
    }

    // uniform random in [a,b)
    public static float randf(float a, float b) {
        return ((b - a) * ((float) rand() / RAND_MAX)) + a;
    }

    public static int randi(int a, int b) {
        return (rand() % (b - a)) + a;
    }

    static boolean deviateAvailable = false;    //	flag
    static float storedDeviate;            //	deviate from previous calculation

    public static double randn(double mu, double sigma) {
        double polar, rsquared, var1, var2;
        if (!deviateAvailable) {
            do {
                var1 = 2.0 * ((double) (rand()) / (double) (RAND_MAX)) - 1.0;
                var2 = 2.0 * ((double) (rand()) / (double) (RAND_MAX)) - 1.0;
                rsquared = var1 * var1 + var2 * var2;
            } while (rsquared >= 1.0 || rsquared == 0.0);
            polar = Math.sqrt(-2.0 * Math.log(rsquared) / rsquared);
            storedDeviate = (float) (var1 * polar);
            deviateAvailable = true;
            return var2 * polar * sigma + mu;
        } else {
            deviateAvailable = false;
            return storedDeviate * sigma + mu;
        }
    }

    //cap value between 0 and 1
    public static float cap(float a) {
        if (a < 0) return 0;
        if (a > 1) return 1;
        return a;
    }
}

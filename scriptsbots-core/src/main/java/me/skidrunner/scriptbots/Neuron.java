package me.skidrunner.scriptbots;

/**
 * An <code>Neuron</code> is a mathematical function conceived as a model of biological neurons. Artificial neurons are
 * the constitutive units in an <code>NeuralNetwork</code>.
 *
 * @author Mark E. Picknell
 */
public class Neuron {

    /**
     * Excitation function type for neurons.
     */
    public enum Type {
        /**
         * Use ELU(Exponential Linear Unit) function
         */
        AFFERENT,
        /**
         * Use binary step function
         */
        EFFERENT,
        /**
         * Use sigmoid function
         */
        INTER
    }

    public int connectionCount;

    public float[] weights;
    public int[] indexes;
    public float bias;

    public Type type;

    /**
     * Constructs an empty Neuron.
     */
    public Neuron() {
    }

    /**
     * Constructs a random neuron.
     * @param connectionCount number of weights and indexes this neuron contains.
     * @param minIndex the min index value that can be assigned to indexes.
     * @param maxIndex the max index value that can be assigned to indexes.
     * @param type the function type to be used when calculating output.
     */
    public Neuron(int connectionCount, int minIndex, int maxIndex, Type type) {
        this.connectionCount = connectionCount;
        this.weights = new float[connectionCount];
        this.indexes = new int[connectionCount];
        this.bias = (float) Math.random();
        this.type = type;

        for(int i = 0; i < connectionCount; i++) {
            this.weights[i] = -1.0f + (float) (Math.random() * 2.0);
            this.indexes[i] = minIndex + (int) (Math.random() * (maxIndex - minIndex));
        }
    }

    /**
     * processes inputs using function associated with this neurons <code>Neuron.Type</code>.
     * @param inputs inputs to be processed.
     * @return transformed excitation.
     */
    public float process(float[] inputs) {
        double excitation = 0;

        // calculate excitation based on sum of input scaled by weights.
        for(int i = 0; i < connectionCount; i++) {
            excitation += inputs[indexes[i]] * weights[i];
        }

        // apply bias to excitation.
        excitation *= bias;

        switch(type) {
            case AFFERENT:
                // ELU(Exponential Linear Unit) function.
                return (Double.compare(excitation, 0) >= 0) ? (float) excitation : (float) Math.exp(excitation);
            case EFFERENT:
                // Binary step function.
                return Double.compare(excitation, 0) >= 0 ? 1.0f : 0.0f;
            case INTER:
            default:
                // Sigmoid function.
                return (1 / (1 + (float) Math.pow(Math.E, (-1 * excitation))));
        }
    }

}

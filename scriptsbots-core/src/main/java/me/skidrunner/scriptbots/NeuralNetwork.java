package me.skidrunner.scriptbots;

/**
 * artificial neural networks (ANNs) are a family of models inspired by biological neural networks (the central nervous
 * systems of animals, in particular the brain) which are used to estimate or approximate functions that can depend on a
 * large number of inputs and are generally unknown
 * @author Mark E. Picknell
 */
public class NeuralNetwork {

    public int inputCount;
    public int outputCount;
    public int neuronCount;

    public Neuron[] neurons;

    private float[] oldMemory;
    private float[] newMemory;

    /**
     * Constructs an empty Neuron.
     */
    public NeuralNetwork() {

    }

    /**
     * Constructs a random NeuralNetwork.
     * @param inputCount number of neurons in network
     * @param outputCount number of raw output values this network will generate.
     */
    public NeuralNetwork(int inputCount, int outputCount) {
        this.inputCount = inputCount;
        this.outputCount = outputCount;
        this.neuronCount = inputCount + outputCount + (int) (Math.random() * ((inputCount + outputCount) * 2));

        this.neurons = new Neuron[neuronCount];

        this.oldMemory = new float[inputCount + neuronCount];
        this.newMemory = new float[neuronCount];

        for(int i = 0; i < neurons.length; i++) {
            if(i < inputCount) {
                this.neurons[i] = new Neuron((int) (inputCount * 0.2), 0, inputCount, Neuron.Type.AFFERENT);
            } else if(i < neuronCount - outputCount) {
                this.neurons[i] = new Neuron((int)((neuronCount - outputCount) * 0.2), inputCount,
                        inputCount + (neuronCount - outputCount), Neuron.Type.INTER);
            } else {
                this.neurons[i] = new Neuron((int) (outputCount * 0.2), inputCount,
                        inputCount + (neuronCount - outputCount), Neuron.Type.EFFERENT);
            }
        }
    }

    /**
     * processes inputs by calling the process function of its neurons by passing excitation from previous process and
     * new inputs.
     * @param inputs input values to add to input map
     * @param outputs array to hold new outputs.
     * @return outputs.
     */
    public float[] process(float[] inputs, float[] outputs) {
        System.arraycopy(inputs, 0, oldMemory, 0, inputCount);
        for(int i = 0; i < neurons.length; i++) {
            newMemory[i] = neurons[i].process(oldMemory);
        }
        System.arraycopy(newMemory, neuronCount - outputCount, outputs, 0, outputCount);
        System.arraycopy(newMemory, 0, oldMemory, inputCount, neuronCount);
        return outputs;
    }

}

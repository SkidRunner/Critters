package me.skidrunner.critters;

import static me.skidrunner.critters.Helper.*;

public class NeuralNetwork implements  java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public static final int INPUT_COUNT = 20;
    public static final int OUTPUT_COUNT = 9;
    public static final int NEURON_COUNT = 100;

    public final Neuron[] neurons = new Neuron[NEURON_COUNT];

    public final float[] inputs = new float[INPUT_COUNT];
    public final float[] outputs = new float[OUTPUT_COUNT];

    private final float[] mInputs = new float[INPUT_COUNT + NEURON_COUNT];
    private final float[] mOutputs = new float[NEURON_COUNT];

    public NeuralNetwork() {
        for(int index = 0; index < NEURON_COUNT; index++) {
            neurons[index] = new Neuron();
        }
    }

    public void set(NeuralNetwork neuralNetwork) {
        for(int index = 0; index < NEURON_COUNT; index++) {
            neurons[index].set(neuralNetwork.neurons[index]);
        }
    }

    public void process() {
        System.arraycopy(inputs, 0, mInputs, 0, INPUT_COUNT);
        for(int index = 0; index < NEURON_COUNT; index++) {
            mOutputs[index] = neurons[index].process(mInputs);
        }
        System.arraycopy(mOutputs, 0, mInputs, INPUT_COUNT, NEURON_COUNT);
        System.arraycopy(mOutputs, NEURON_COUNT - OUTPUT_COUNT, outputs, 0, OUTPUT_COUNT);
    }

    public void randomize() {
        for(int index = 0; index < NEURON_COUNT; index++) {
            neurons[index].randomize();
        }
    }

    public void cross(NeuralNetwork neuralNetworkA, NeuralNetwork neuralNetworkB) {
        for(int index = 0; index < NEURON_COUNT; index++) {
            neurons[index].cross(neuralNetworkA.neurons[index], neuralNetworkB.neurons[index]);
        }
    }

    public void mutate(double rateA, double rateB) {
        for(int index = 0; index < NEURON_COUNT; index++) {
            neurons[index].mutate(rateA, rateB);
        }
    }

    public static class Neuron implements  java.io.Serializable {
        private static final long serialVersionUID = 1L;

        public static final int CONNECTION_COUNT = 3;

        public final Connection[] connections = new Connection[CONNECTION_COUNT];

        public Function function = Function.AND;

        public float dampingStrength;
        public float output;
        public float bias;

        public Neuron() {
            for(int index = 0; index < CONNECTION_COUNT; index++) {
                connections[index] = new Connection();
            }
        }

        public void set(Neuron neuron) {
            for(int index = 0; index < CONNECTION_COUNT; index++) {
                connections[index].set(neuron.connections[index]);
            }
            dampingStrength = neuron.dampingStrength;
            bias = neuron.bias;
        }

        public float process(float[] inputs) {
            float activity = function.exec(this, inputs);
            output += (activity - output) * dampingStrength;
            return output;
        }

        public void randomize() {
            for(int index = 0; index < CONNECTION_COUNT; index++) {
                connections[index].randomize();
            }
            function = Function.random();
            dampingStrength = Random.nextFloat(0.8F, 1F);
            bias = Random.nextFloat(-1F, 1F);
        }

        public void cross(Neuron neuronA, Neuron neuronB) {
            for(int index = 0; index < CONNECTION_COUNT; index++) {
                connections[index].cross(neuronA.connections[index], neuronB.connections[index]);
            }
            function = Random.nextChoice(neuronA.function, neuronB.function);
            dampingStrength = Random.nextChoice(neuronA.dampingStrength, neuronB.dampingStrength);
            bias = Random.nextChoice(neuronA.bias, neuronB.bias);
        }

        public void mutate(double rateA, double rateB) {
            if(Random.nextBoolean(rateA * 3D)) {
                bias += Random.nextNormal(0, rateB);
            }

            /*
            if(Random.nextBoolean(rateA * 3D)) {
                dampingStrength = Math.max(0.1F, Math.min(1F, dampingStrength + Random.nextNormal(0, rateB)));
            }
            */

            if(Random.nextBoolean(rateA * 3D)) {
                Connection connection = connections[Random.nextInt(0, CONNECTION_COUNT - 1)];
                connection.strength = MathF.max(0.1D, connection.strength + Random.nextNormal(0, rateB));
            }

            if(Random.nextBoolean(rateA)) {
                Connection connection = connections[Random.nextInt(0, CONNECTION_COUNT - 1)];
                connection.index = Random.nextInt(0, NEURON_COUNT - 1);
            }

            if(Random.nextBoolean(rateA)) {
                Connection connection = connections[Random.nextInt(0, CONNECTION_COUNT - 1)];
                connection.not = !connection.not;
            }

            if(Random.nextBoolean(rateA)) {
                function = Function.random();
            }

        }

        public static class Connection implements  java.io.Serializable {
            private static final long serialVersionUID = 1L;

            public volatile int index;
            public volatile float strength;
            public volatile boolean not;

            public Connection() { }

            public void set(Connection connection) {
                index = connection.index;
                strength = connection.strength;
                not = connection.not;
            }

            public float getInput(float[] inputs) {
                return not ? 1 - inputs[index] : inputs[index];
            }

            public float getValue(float[] inputs) {
                return strength * getInput(inputs);
            }

            public void randomize() {
                if(Random.nextBoolean(0.2F)) {
                    index = Random.nextInt(0, INPUT_COUNT - 1);
                } else {
                    index = Random.nextInt(0, INPUT_COUNT + NEURON_COUNT - 1);
                }
                strength = Random.nextFloat(0.1F, 1F);
                not = Random.nextBoolean();
            }

            public void cross(Connection connectionA, Connection connectionB) {
                index = Random.nextChoice(connectionA.index, connectionB.index);
                strength = Random.nextChoice(connectionA.strength, connectionB.strength);
                not = Random.nextChoice(connectionA.not, connectionB.not);
            }

        }

        public enum Function {
            AND(new FunctionProcess() {
                @Override
                float process(Connection connection, float[] inputs, float activity) {
                    return activity * connection.getInput(inputs);
                }

                @Override
                float process(Neuron neuron, float activity) {
                    return activity * neuron.bias;
                }
            }),
            OR(new FunctionProcess() {
                @Override
                float process(Connection connection, float[] inputs, float activity) {
                    return activity + connection.getValue(inputs);
                }

                @Override
                float process(Neuron neuron, float activity) {
                    return activity + neuron.bias;
                }
            });

            private final FunctionProcess functionProcess;

            Function(FunctionProcess functionProcess) {
                this.functionProcess = functionProcess;
            }

            public float exec(Neuron neuron, float[] inputs) {
                return functionProcess.exec(neuron, inputs);
            }

            public static Function random() {
                return values()[Random.nextInt(0, values().length - 1)];
            }

        }

        private abstract static class FunctionProcess implements  java.io.Serializable {
            private static final long serialVersionUID = 1L;

            public float exec(Neuron neuron, float[] inputs) {
                float activity = 1;
                for(int index = 0; index < CONNECTION_COUNT; index++) {
                    activity = process(neuron.connections[index], inputs, activity);
                }
                activity = process(neuron, activity);
                return MathF.clamp(0F, 1F, activity);
            }

            abstract float process(Connection connection, float[] inputs, float activity);

            abstract float process(Neuron neuron, float activity);
        }
    }

}

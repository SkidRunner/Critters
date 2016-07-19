package com.skidrunner.scriptbots;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Mark E. Picknell
 */
public class AssemblyBrain implements Cloneable, Serializable {

    public final float[] weights;

    public AssemblyBrain(int size) {
        weights = new float[size];
    }

    public AssemblyBrain(AssemblyBrain other) {
        weights = new float[other.weights.length];
        System.arraycopy(other.weights, 0, weights, 0, weights.length);
    }

    public void process(float[] inputs, float[] outputs) {
        for(int i = 0; i < inputs.length; i++) {
            weights[i] = inputs[i];
        }

        for(int i = inputs.length; i < weights.length - outputs.length; i++) {

            if(weights[i] >= 2 && weights[i] < 3) {
                float weight1 = weights[i + 1];
                float weight2 = weights[i + 2];
                float weight3 = weights[i + 3];

                int index1 = (int) (weights.length * (Math.abs(weight1) - ((int) Math.abs(weight1))));
                int index2 = (int) (weights.length * (Math.abs(weight2) - ((int) Math.abs(weight3))));
                int index3 = (int) (weights.length * (Math.abs(weight3) - ((int) Math.abs(weight3))));

                if(weights[i] < 2.1) {
                    weights[index3] = weights[index1] + weights[index2];
                    continue;
                }

                if(weights[i] < 2.2) {
                    weights[index3] = weights[index1] - weights[index2];
                    continue;
                }

                if(weights[i] < 2.3) {
                    weights[index3] = weights[index1] * weights[index2];
                    continue;
                }

                if(weights[i] < 2.4) {
                    if(weights[index3] > 0) {
                        weights[index1] = 0;
                    }
                    continue;
                }

                if(weights[i] < 2.5) {
                    if(weights[index3] > 0) {
                        weights[index1] = -weights[index1];
                    }
                    continue;
                }

                if(weights[i] < 2.7) {
                    if(weights[index3] > 0) {
                        weights[index1] += weight2;
                    }
                    continue;
                }

                if(weights[i] < 3) {
                    if(weights[index3] > 0) {
                        weights[index1] = weights[index2];
                    }
                    continue;
                }
            }
        }

        for(int i = inputs.length; i < weights.length - outputs.length; i++) {
            weights[i] = Math.max(-10, Math.min(10, weights[i]));
        }

        for(int i = 0; i < outputs.length; i++) {
            outputs[i] = Math.max(0, Math.min(1, weights[weights.length - 1 - i]));
        }
    }

    public void mutate(float rate, float frequency) {
        for(int i = 0; i < weights.length; i++) {
            if(Math.random() > rate) {
                weights[i] = (float) ((Math.random() * 6) - 3);
            }
        }
    }

    public AssemblyBrain cross(AssemblyBrain assemblyBrain) {
        if(this.weights.length != assemblyBrain.weights.length) {
            throw new RuntimeException("Incompatible brain sizes");
        }

        AssemblyBrain newAssemblyBrain = assemblyBrain.clone();
        float[] newWeights = newAssemblyBrain.weights;

        for(int i = 0; i < newWeights.length; i++) {
            if(Math.random() < 0.5) {
                newWeights[i] = weights[i];
            }
        }

        return newAssemblyBrain;
    }

    @Override
    public int hashCode() {
        int hashCode = 42;
        hashCode += 42 * Arrays.hashCode(weights);
        return hashCode;
    }

    @Override
    public AssemblyBrain clone() {
        try {
            return (AssemblyBrain) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof AssemblyBrain)) {
            return false;
        }
        AssemblyBrain assemblyBrain = (AssemblyBrain) object;
        if(this == assemblyBrain) return true;
        if(!Arrays.equals(weights, assemblyBrain.weights)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "{ \"weights\" : " + Arrays.toString(weights) + " }";
    }

    public static AssemblyBrain NewRandom(int size) {
        AssemblyBrain newAssemblyBrain = new AssemblyBrain(size);
        float[] newWeights = newAssemblyBrain.weights;

        for(int i = 0; i < newWeights.length; i++) {
            newWeights[i] = (float) ((Math.random() * 6) - 3);
            if(Math.random() > 0.1) newWeights[i] = (float) (Math.random() * 0.5);
            if(Math.random() > 0.1) newWeights[i] = (float) ((Math.random() * 0.2) + 0.8);
        }

        return newAssemblyBrain;
    }
}

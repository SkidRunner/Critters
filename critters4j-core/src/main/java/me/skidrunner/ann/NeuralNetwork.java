package me.skidrunner.ann;

public class NeuralNetwork {
	
	private int id;
	private int inputCount;
	private int outputCount;
	private Vector<Neuron> neurons;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getInputCount() {
		return inputCount;
	}
	
	public void setInputCount(int inputCount) {
		this.inputCount = inputCount;
	}
	
	public int getOutputCount() {
		return outputCount;
	}
	
	public void setOutputCount(int outputCount) {
		this.outputCount = outputCount;
	}
	
	public Vector<Neuron> getNeurons() {
		return neurons;
	}
	
	public void setNeurons(Vector<Neuron> neurons) {
		this.neurons = neurons;
	}
	
}

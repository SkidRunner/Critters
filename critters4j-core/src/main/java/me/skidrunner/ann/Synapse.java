package me.skidrunner.ann;

public class Synapse {
	
	private int id;
	private int neuronId;
	private float weight;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getNeuronId() {
		return neuronId;
	}
	
	public void setNeuronId(int neuronId) {
		this.neuronId = neuronId;
	}
	
	public float getWeight() {
		return weight;
	}
	
	public void setWeight(float weight) {
		this.weight = weight;
	}
	
}

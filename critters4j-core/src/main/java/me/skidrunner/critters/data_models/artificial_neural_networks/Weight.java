package me.skidrunner.critters.data_models.artificial_neural_networks;

public class Weight {
	
	private int transferNeuronId;
	private int activationNeuronId;
	private float value;
	
	public int getTransferNeuronId() {
		return this.transferNeuronId;
	}
	
	public void setTransferNeuronId(int transferNeuronId) {
		this.transferNeuronId = transferNeuronId;
	}
	
	public int getActivationNeuronId() {
		return this.activationNeuronId;
	}
	
	public void setActivationNeuronId(int activationNeuronId) {
		this.activationNeuronId = activationNeuronId;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public void setValue(float value) {
		this.value = value;
	}
	
}

package me.skidrunner.ann;

public class Neuron {
	
	private int id;
	private Vector<Synapse> synapses;
	private Function transferFunction;
	private Function activationFunction;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Vector<Synapse> getSynapses() {
		return synapses;
	}
	
	public void setSynapses(Vector<Synapse> synapses) {
		this.synapses = synapses;
	}
	
	public Function getTransferFunction() {
		return transferFunction;
	}
	
	public void setTransferFunction(Function transferFunction) {
		this.transferFunction = transferFunction;
	}
	
	public Function getActivationFunction() {
		return activationFunction;
	}
	
	public void setActivationFunction(Function activationFunction) {
		this.activationFunction = activationFunction;
	}
	
}

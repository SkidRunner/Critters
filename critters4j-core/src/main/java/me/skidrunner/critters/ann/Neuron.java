package me.skidrunner.critters.ann;

public class Neuron {
  
  private float input;
  private float activation;
  private float output;
  
  public void setInput(float input) {
    this.input = input;
  }
  
  public float getInput() {
    return input;
  }
  
  public float getActivation() {
    return activation;
  }
  
  public void setActivation(float activation) {
    this.activation = activation;
  }
  
  public float getOutput() {
    return output;
  }
  
  public void setOuput(float output) {
    this.output = output;
  }
  
}

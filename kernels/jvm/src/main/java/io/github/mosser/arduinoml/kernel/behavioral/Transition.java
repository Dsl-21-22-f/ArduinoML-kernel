package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitable;
import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.*;

import java.util.List;

public class Transition implements Visitable {

	private State next;

	private TimeCondition timeCondition;

	private List<SensorCondition> sensorConditions;


	public State getNext() {
		return next;
	}

	public void setNext(State next) {
		this.next = next;
	}

	public TimeCondition getTimeCondition() {
		return timeCondition;
	}

	public void setTimeCondition(TimeCondition timeCondition) {
		this.timeCondition = timeCondition;
	}

	public List<SensorCondition> getSensorConditions() {
		return sensorConditions;
	}

	public void addSensorCondition(SensorCondition sensorCondition){
		if(sensorCondition!=null){
			this.sensorConditions.add(sensorCondition);
		}
	}
	public void setSensorConditions(List<SensorCondition> sensorConditions) {
		this.sensorConditions = sensorConditions;
	}



	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}

package main.groovy.groovuinoml.dsl

import io.github.mosser.arduinoml.kernel.behavioral.CONDITION
import io.github.mosser.arduinoml.kernel.behavioral.SensorCondition
import io.github.mosser.arduinoml.kernel.behavioral.TimeCondition
import main.groovy.groovuinoml.dsl.GroovuinoMLBinding

import java.util.List;

import io.github.mosser.arduinoml.kernel.behavioral.Action
import io.github.mosser.arduinoml.kernel.behavioral.State
import io.github.mosser.arduinoml.kernel.structural.Actuator
import io.github.mosser.arduinoml.kernel.structural.Sensor
import io.github.mosser.arduinoml.kernel.structural.SIGNAL

abstract class GroovuinoMLBasescript extends Script {
	// sensor "name" pin n
	def sensor(String name) {
		[pin: { n -> ((GroovuinoMLBinding)this.getBinding()).getGroovuinoMLModel().createSensor(name, n) },
		onPin: { n -> ((GroovuinoMLBinding)this.getBinding()).getGroovuinoMLModel().createSensor(name, n)}]
	}
	
	// actuator "name" pin n
	def actuator(String name) {
		[pin: { n -> ((GroovuinoMLBinding)this.getBinding()).getGroovuinoMLModel().createActuator(name, n) }]
	}
	
	// state "name" means actuator becomes signal [and actuator becomes signal]*n
	def state(String name) {
		List<Action> actions = new ArrayList<Action>()
		((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createState(name, actions)
		// recursive closure to allow multiple and statements
		def closure
		closure = { actuator -> 
			[becomes: { signal ->
				Action action = new Action()
				action.setActuator(actuator instanceof String ? (Actuator)((GroovuinoMLBinding)this.getBinding()).getVariable(actuator) : (Actuator)actuator)
				action.setValue(signal instanceof String ? (SIGNAL)((GroovuinoMLBinding)this.getBinding()).getVariable(signal) : (SIGNAL)signal)
				actions.add(action)
				[and: closure]
			}]
		}
		[means: closure]
	}
	
	// initial state
	def initial(state) {
		((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().setInitialState(state instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state) : (State)state)
	}
	
	// from state1 to state2 when sensor becomes signal
	def from(state1) {
		int nb_Transition = -1
		List<List<SensorCondition>> sensorConditions = new ArrayList<List<SensorCondition>>();
		List<TimeCondition> timeConditions = new ArrayList<TimeCondition>()
		[to: { state2 ->
			def newTransition
			newTransition = {
				nb_Transition++
				sensorConditions.add(new ArrayList<SensorCondition>())
				((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createTransition(
						state1 instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state1) : (State)state1,
						state2 instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state2) : (State)state2,
						sensorConditions[nb_Transition],
						timeConditions[nb_Transition])
			}
			newTransition()
			def closure
			closure = { sensor1 ->
				[becomes: { signal1 ->
					SensorCondition sensorCond = new SensorCondition();
					sensorCond.setSensor(sensor1 instanceof String ? (Sensor) ((GroovuinoMLBinding) this.getBinding()).getVariable(sensor1) : (Sensor) sensor1)
					sensorCond.setValue(signal1 instanceof String ? (CONDITION) ((GroovuinoMLBinding) this.getBinding()).getVariable(signal1) : (CONDITION) signal1)
					sensorConditions[nb_Transition].add(sensorCond)
					[and: closure,
					 or : { signal2 ->
						 newTransition()
						 closure(signal2)
					 }]
				}]
			}
			[when: closure,
			 after : { time ->
				 timeConditions.add(new TimeCondition())
				 timeConditions[nb_Transition].setTime(Integer.parseInt(time))
				 [and: closure]
			 }]
		}]
	}
	
	// export name
	def export(String name) {
		println(((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().generateCode(name).toString())
	}
	
	// disable run method while running
	int count = 0
	abstract void scriptBody()
	def run() {
		if(count == 0) {
			count++
			scriptBody()
		} else {
			println "Run method is disabled"
		}
	}
}

package main.groovy.groovuinoml.dsl

import io.github.mosser.arduinoml.kernel.behavioral.BinaryExpr
import io.github.mosser.arduinoml.kernel.behavioral.CONDITION
import io.github.mosser.arduinoml.kernel.behavioral.OPERATOR
import io.github.mosser.arduinoml.kernel.behavioral.UnaryExpr

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
		[to: { state2 -> 
			[when: { sensor1 ->
				Boolean containsAndOr = false
				[becomes: { signal1 ->
					UnaryExpr expr1= new UnaryExpr();
					expr1.setSensor(
							sensor1 instanceof String ? (Sensor)((GroovuinoMLBinding)this.getBinding()).getVariable(sensor1) : (Sensor)sensor1)
					expr1.setValue(
							signal1 instanceof String ? (CONDITION)((GroovuinoMLBinding)this.getBinding()).getVariable(signal1) : (CONDITION)signal1)
					/*
					[and: { sensor2 ->
						containsAndOr = true
						[becomes: { signal2 ->
							UnaryExpr expr2= new UnaryExpr()
							expr2.setSensor(
									sensor1 instanceof String ? (Sensor)((GroovuinoMLBinding)this.getBinding()).getVariable(sensor2) : (Sensor)sensor2)
							expr2.setValue(
									signal2 instanceof String ? (SIGNAL)((GroovuinoMLBinding)this.getBinding()).getVariable(signal2) : (SIGNAL)signal2)
							BinaryExpr binExpr = new BinaryExpr()
							binExpr.setLeft(expr1)
							binExpr.setRight(expr2)
							binExpr.operator(OPERATOR.AND)
							((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createTransition(
									state1 instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state1) : (State)state1,
									state2 instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state2) : (State)state2,
									binExpr)
						}]
					}]*/
					/*[or: { sensor2 ->
						containsAndOr = true
						[becomes: { signal2 ->
							UnaryExpr expr2= new UnaryExpr()
							expr2.setSensor(
									sensor1 instanceof String ? (Sensor)((GroovuinoMLBinding)this.getBinding()).getVariable(sensor2) : (Sensor)sensor2)
							expr2.setValue(
									signal2 instanceof String ? (SIGNAL)((GroovuinoMLBinding)this.getBinding()).getVariable(signal2) : (SIGNAL)signal2)
							BinaryExpr binExpr = new BinaryExpr()
							binExpr.setLeft(expr1)
							binExpr.setRight(expr2)
							binExpr.operator(OPERATOR.OR)
							((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createTransition(
									state1 instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state1) : (State)state1,
									state2 instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state2) : (State)state2,
									binExpr)
						}]
					}]*/
					if(!containsAndOr){
						((GroovuinoMLBinding) this.getBinding()).getGroovuinoMLModel().createTransition(
								state1 instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state1) : (State)state1,
								state2 instanceof String ? (State)((GroovuinoMLBinding)this.getBinding()).getVariable(state2) : (State)state2,
								expr1)
					}
				}]
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

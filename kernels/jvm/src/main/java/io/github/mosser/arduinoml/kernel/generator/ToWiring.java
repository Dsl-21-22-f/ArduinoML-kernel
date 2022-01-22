package io.github.mosser.arduinoml.kernel.generator;

import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.*;
import io.github.mosser.arduinoml.kernel.structural.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Quick and dirty visitor to support the generation of Wiring code
 */
public class ToWiring extends Visitor<StringBuffer> {
	enum PASS {ONE, TWO, THREE, FOUR, FIVE, SIX}


	public ToWiring() {
		this.result = new StringBuffer();
	}

	private void w(String s) {
		result.append(String.format("%s",s));
	}

	@Override
	public void visit(App app) {
		//first pass, create global vars
		context.put("pass", PASS.ONE);
		w("// Wiring code generated from an ArduinoML model\n");
		w(String.format("// Application name: %s\n", app.getName())+"\n");

		w("long debounce = 200;\n");
		w("\nenum STATE {");
		String sep ="";
		for(State state: app.getStates()){
			w(sep);
			state.accept(this);
			sep=", ";
		}
		w("};\n");
		if (app.getInitial() != null) {
			w("STATE currentState = " + app.getInitial().getName()+";\n");
		}

		for(Brick brick: app.getBricks()){
			brick.accept(this);
		}
		w("\nlong timer = 0;\n");

		//second pass, setup and loop
		context.put("pass",PASS.TWO);
		w("\nvoid setup(){\n");
		for(Brick brick: app.getBricks()){
			brick.accept(this);
		}
		w("\n  timer = millis();\n");

		w("}\n");

		w("\nvoid loop() {\n");

		context.put("pass",PASS.THREE);
		for(Brick brick: app.getBricks()){
			brick.accept(this);
		}
		w("\tswitch(currentState){\n");
		context.put("pass",PASS.FOUR);
		for(State state: app.getStates()){
			state.accept(this);
		}
		w("\t}\n");
		context.put("pass",PASS.FIVE);
		for(Brick brick: app.getBricks()){
			brick.accept(this);
		}
		w("}");
	}

	@Override
	public void visit(Actuator actuator) {
		if(context.get("pass") == PASS.ONE) {
			return;
		}
		if(context.get("pass") == PASS.TWO) {
			w(String.format("  pinMode(%d, OUTPUT); // %s [Actuator]\n", actuator.getPin(), actuator.getName()));
			return;
		}
	}


	@Override
	public void visit(Sensor sensor) {
		if(context.get("pass") == PASS.ONE) {
			w(String.format("\nboolean %sBounceGuard = false;\n", sensor.getName()));
			w(String.format("long %sLastDebounceTime = 0;\n", sensor.getName()));

			w(String.format("\nlong %sState = LOW;\n", sensor.getName()));
			w(String.format("long %sLastState = LOW;\n", sensor.getName()));
			return;
		}
		if(context.get("pass") == PASS.TWO) {
			w(String.format("  pinMode(%d, INPUT);  // %s [Sensor]\n", sensor.getPin(), sensor.getName()));
			return;
		}
		if(context.get("pass") == PASS.THREE) {
			w(String.format("   %sState = digitalRead(%d);\n", sensor.getName(), sensor.getPin()));
			return;
		}

		if(context.get("pass") == PASS.FIVE) {
			w(String.format("   %sLastState = %sState;\n", sensor.getName(),sensor.getName() ));
			return;
		}
	}

	@Override
	public void visit(UnaryExpr unaryExpr) {
		unaryExpr.getCondition().accept(this);
	}

	@Override
	public void visit(BinaryExpr binaryExpr) {
		binaryExpr.getLeft().accept(this);
		if (context.get("pass") == PASS.FOUR){
			//OPERATOR
			w(String.format(" %s ", binaryExpr.getOperator().getValue()));
		}
		binaryExpr.getRight().accept(this);
	}

	@Override
	public void visit(TimeCondition timeCondition) {

		if(context.get("pass") == PASS.FOUR) {
			w(String.format("(millis() - timer > %d)",
					timeCondition.getTime()));
		}
		if(context.get("pass") == PASS.FIVE) {

		}
	}

	@Override
	public void visit(SensorCondition sensorCondition) {
		if(context.get("pass") == PASS.FOUR) {
			if(sensorCondition.getValue()==CONDITION.PUSHED){
				printPushedCondition(sensorCondition.getSensor().getName());
			}
			else{
				printSignalCondition(sensorCondition.getSensor(), sensorCondition.getValue());
			}
		}
		if(context.get("pass") == PASS.FIVE) {
			sensorCondition.getSensor().accept(this);
		}

		}


	@Override
	public void visit(State state) {
		if(context.get("pass") == PASS.ONE){
			w(state.getName());
			return;
		}
		if(context.get("pass") == PASS.FOUR) {
			w("\t\tcase " + state.getName() + ":\n");
			for (Action action : state.getActions()) {
				action.accept(this);
			}
			for (Transition transition : state.getTransitions()) {
				transition.accept(this);
			}
			w("\t\tbreak;\n");
			return;
		}

	}


	@Override
	public void visit(Transition transition) {
		if(context.get("pass") == PASS.FOUR) {
			Expr expr = transition.getExpr();
			if (expr.getExprType() == ExprType.UNARY) {
				if((((UnaryExpr) expr).getCondition()).getType()==ConditionType.SENSOR){
					String sensorName = ((SensorCondition) ((UnaryExpr) expr).getCondition()).getSensor().getName();
					printDebounceGuard(sensorName);
				}

			} else {
				if((((BinaryExpr) expr).getLeft().getCondition()).getType()==ConditionType.SENSOR){
					String sensorName = ((SensorCondition) ((BinaryExpr) expr).getLeft().getCondition()).getSensor().getName();
					printDebounceGuard(sensorName);
				}
				if((((BinaryExpr) expr).getRight().getCondition()).getType()==ConditionType.SENSOR){
					String sensorName2 = ((SensorCondition) ((BinaryExpr) expr).getRight().getCondition()).getSensor().getName();
					printDebounceGuard(sensorName2);
				}



			}
			//Conditions
			w(String.format("\t\t\tif("));
			expr.accept(this);
			w(String.format("){\n"));
			if(expr.getExprType() == ExprType.UNARY){
				if((((UnaryExpr) expr).getCondition()).getType()==ConditionType.SENSOR) {
					printDebounceButton(((UnaryExpr) expr));
				}
			}
			else{
				if((((BinaryExpr) expr).getLeft().getCondition()).getType()==ConditionType.SENSOR) {

					printDebounceButton(((BinaryExpr) expr).getLeft());
				}
				if((((BinaryExpr) expr).getRight().getCondition()).getType()==ConditionType.SENSOR) {

					printDebounceButton(((BinaryExpr) expr).getRight());
				}
			}
			w("\t\t\t\tcurrentState = " + transition.getNext().getName() + ";\n");
			w("\t\t\t\ttimer = millis();\n");

			w("\t\t\t}\n");
			return;
		}
	}

	void printDebounceButton(UnaryExpr expr){
		if(((SensorCondition) expr.getCondition()).getSensor()!=null){
			w(String.format("\t\t\t\t%sLastDebounceTime = millis();\n", ((SensorCondition) expr.getCondition()).getSensor().getName()));
		}

	}

	void printPushedCondition(String sensorName){
		w(String.format("   (%sLastState != %sState && %sBounceGuard && %sState == HIGH)", sensorName,sensorName,sensorName,sensorName));
	}

	void printSignalCondition(Sensor sensor, CONDITION signal){
		w(String.format("(digitalRead(%d) == %s && %sBounceGuard)",
				sensor.getPin(), signal, sensor.getName()));
	}

	void printDebounceGuard(String sensorName){
		w(String.format("\t\t\t%sBounceGuard = millis() - %sLastDebounceTime > debounce;\n",
				sensorName, sensorName));
	}


	@Override
	public void visit(Action action) {
		if(context.get("pass") == PASS.ONE) {
			return;
		}
		if(context.get("pass") == PASS.FOUR) {
			w(String.format("\t\t\tdigitalWrite(%d,%s);\n",action.getActuator().getPin(),action.getValue()));
			return;
		}
	}

}

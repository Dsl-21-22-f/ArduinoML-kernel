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
	enum PASS {ONE, TWO, THREE, FOUR}


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
		//second pass, setup and loop
		context.put("pass",PASS.TWO);
		w("\nvoid setup(){\n");
		for(Brick brick: app.getBricks()){
			brick.accept(this);
		}
		w("}\n");

		w("\nvoid loop() {\n");

		context.put("pass",PASS.THREE);
		for(Brick brick: app.getBricks()){
			brick.accept(this);
		}
		w("\tswitch(currentState){\n");
		for(State state: app.getStates()){
			state.accept(this);
		}
		w("\t}\n");
		context.put("pass",PASS.FOUR);
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
		if(context.get("pass") == PASS.FOUR) {
			w(String.format("   %sLastState = %sState;\n", sensor.getName(),sensor.getName() ));
			return;
		}
	}



	@Override
	public void visit(State state) {
		if(context.get("pass") == PASS.ONE){
			w(state.getName());
			return;
		}
		if(context.get("pass") == PASS.THREE) {
			w("\t\tcase " + state.getName() + ":\n");
			for (Action action : state.getActions()) {
				action.accept(this);
			}

			if (state.getTransition() != null) {
				state.getTransition().accept(this);
				w("\t\tbreak;\n");
			}
			return;
		}

	}

	@Override
	public void visit(Transition transition) {
		if(context.get("pass") == PASS.ONE) {
			return;
		}
		if(context.get("pass") == PASS.THREE) {
			Expr expr = transition.getExpr();
			System.out.println(expr.getExprType());
			if(expr.getExprType()==ExprType.UNARY){
				String sensorName = ((UnaryExpr) expr).getSensor().getName();
				printDebounceGuard(sensorName);
			}
			else{
				String sensorName = ((BinaryExpr) expr).getLeft().getSensor().getName();
				printDebounceGuard(sensorName);
				String sensorName2 = ((BinaryExpr) expr).getRight().getSensor().getName();
				printDebounceGuard(sensorName2);
			}
			//
			w(String.format("\t\t\tif("));
			if(expr.getExprType()==ExprType.UNARY){
				String sensorName = ((UnaryExpr) expr).getSensor().getName();
				if(((UnaryExpr)expr).getValue()==SIGNAL.PUSHED){
					printPushedCondition(sensorName);
				}
				else{
					printSignalCondition(((UnaryExpr)expr).getSensor(), ((UnaryExpr)expr).getValue());
				}
			}
			else{
				String sensorNameLeft = ((BinaryExpr) expr).getLeft().getSensor().getName();
				if(((BinaryExpr) expr).getLeft().getValue()==SIGNAL.PUSHED){
					printPushedCondition(sensorNameLeft);
				}
				else{
					printSignalCondition(((BinaryExpr)expr).getLeft().getSensor(), ((BinaryExpr)expr).getLeft().getValue());
				}
				//OPERATOR
				w(String.format(" %s ",
						((BinaryExpr)expr).getOperator().getValue()));
				String sensorNameRight = ((BinaryExpr) expr).getRight().getSensor().getName();

				if(((BinaryExpr) expr).getRight().getValue()==SIGNAL.PUSHED){
					printPushedCondition(sensorNameRight);
				}
				else{
					 printSignalCondition(((BinaryExpr)expr).getRight().getSensor(), ((BinaryExpr)expr).getRight().getValue());
				}


			}
			w(String.format("){\n"));


			w("\t\t\t\tcurrentState = " + transition.getNext().getName() + ";\n");
			w("\t\t\t}\n");
			return;
		}
	}

	void printPushedCondition(String sensorName){
		w(String.format("   (%sLastState != %sState && %sBounceGuard && %sState == HIGH)", sensorName,sensorName,sensorName,sensorName));
	}

	void printSignalCondition(Sensor sensor, SIGNAL signal){
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
		if(context.get("pass") == PASS.THREE) {
			w(String.format("\t\t\tdigitalWrite(%d,%s);\n",action.getActuator().getPin(),action.getValue()));
			return;
		}
	}

}

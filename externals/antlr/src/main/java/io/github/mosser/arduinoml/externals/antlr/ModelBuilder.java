package io.github.mosser.arduinoml.externals.antlr;

import io.github.mosser.arduinoml.externals.antlr.grammar.*;


import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.*;
import io.github.mosser.arduinoml.kernel.structural.Actuator;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

import java.util.HashMap;
import java.util.Map;

public class ModelBuilder extends ArduinomlBaseListener {

    /********************
     ** Business Logic **
     ********************/

    private App theApp = null;
    private boolean built = false;

    public App retrieve() {
        if (built) { return theApp; }
        throw new RuntimeException("Cannot retrieve a model that was not created!");
    }

    /*******************
     ** Symbol tables **
     *******************/

    private Map<String, Sensor>   sensors   = new HashMap<>();
    private Map<String, Actuator> actuators = new HashMap<>();
    private Map<String, State>    states  = new HashMap<>();
    private Map<String, Binding>  bindings  = new HashMap<>();

    private Map<String, BinaryExpr>  binarys  = new HashMap<>();
    private Map<String, UnaryExpr>  unarys  = new HashMap<>();



    private class Binding { // used to support state resolution for transitions
        String to; // name of the next state, as its instance might not have been compiled yet
        Expr expr;
    }



    private State currentState = null;

    /**************************
     ** Listening mechanisms **
     **************************/

    @Override
    public void enterRoot(ArduinomlParser.RootContext ctx) {
        built = false;
        theApp = new App();
    }

    @Override public void exitRoot(ArduinomlParser.RootContext ctx) {
        // Resolving states in transitions
        bindings.forEach((key, binding) ->  {
            Transition t = new Transition();
            t.setExpr(binding.expr);
            t.setNext(states.get(binding.to));
            states.get(key).setTransition(t);
        });
        this.built = true;
    }

    @Override
    public void enterDeclaration(ArduinomlParser.DeclarationContext ctx) {
        theApp.setName(ctx.name.getText());
    }

    @Override
    public void enterSensor(ArduinomlParser.SensorContext ctx) {
        Sensor sensor = new Sensor();
        sensor.setName(ctx.location().id.getText());
        sensor.setPin(Integer.parseInt(ctx.location().port.getText()));
        this.theApp.getBricks().add(sensor);
        sensors.put(sensor.getName(), sensor);
    }

    @Override
    public void enterActuator(ArduinomlParser.ActuatorContext ctx) {
        Actuator actuator = new Actuator();
        actuator.setName(ctx.location().id.getText());
        actuator.setPin(Integer.parseInt(ctx.location().port.getText()));
        this.theApp.getBricks().add(actuator);
        actuators.put(actuator.getName(), actuator);
    }

    @Override
    public void enterState(ArduinomlParser.StateContext ctx) {
        State local = new State();
        local.setName(ctx.name.getText());
        this.currentState = local;
        this.states.put(local.getName(), local);
    }

    @Override
    public void exitState(ArduinomlParser.StateContext ctx) {
        this.theApp.getStates().add(this.currentState);
        this.currentState = null;
    }

    @Override
    public void enterAction(ArduinomlParser.ActionContext ctx) {
        Action action = new Action();
        action.setActuator(actuators.get(ctx.receiver.getText()));
        action.setValue(SIGNAL.valueOf(ctx.value.getText()));
        currentState.getActions().add(action);
    }

    @Override
    public void exitTransition(ArduinomlParser.TransitionContext ctx) {
        // Creating a placeholder as the next state might not have been compiled yet.
        Binding toBeResolvedLater = new Binding();
        toBeResolvedLater.to      = ctx.next.getText();
        if(binarys.get(currentState.getName())!=null){
            toBeResolvedLater.expr    = binarys.get(currentState.getName());
        }
        else{
            toBeResolvedLater.expr    = unarys.get(currentState.getName());
        }
        bindings.put(currentState.getName(), toBeResolvedLater);
    }



    @Override
    public void enterInitial(ArduinomlParser.InitialContext ctx) {
        this.theApp.setInitial(this.currentState);
    }

    @Override public void enterBinaryexpr(ArduinomlParser.BinaryexprContext ctx) {
        BinaryExpr binaryExpr = new BinaryExpr();
        UnaryExpr left = new UnaryExpr();
        UnaryExpr right = new UnaryExpr();

        if( ctx.expr1.condition().sensorcondition()!=null){
            SensorCondition sensorCondition = new SensorCondition();
            sensorCondition.setSensor(sensors.get(ctx.expr1.condition().sensorcondition().trigger.getText()));
            sensorCondition.setValue(CONDITION.valueOf(ctx.expr1.condition().sensorcondition().value.getText()));
            left.setCondition(sensorCondition);
        }
        else{
            TimeCondition timeCondition = new TimeCondition();
            timeCondition.setTime(Integer.parseInt(ctx.expr1.condition().timecondition().trigger.getText()));
            left.setCondition(timeCondition);
        }

        if( ctx.expr2.condition().sensorcondition()!=null){
            SensorCondition sensorCondition = new SensorCondition();
            sensorCondition.setSensor(sensors.get(ctx.expr2.condition().sensorcondition().trigger.getText()));
            sensorCondition.setValue(CONDITION.valueOf(ctx.expr2.condition().sensorcondition().value.getText()));
            right.setCondition(sensorCondition);

        }
        else{
            TimeCondition timeCondition = new TimeCondition();
            timeCondition.setTime(Integer.parseInt(ctx.expr2.condition().timecondition().trigger.getText()));
            right.setCondition(timeCondition);
        }


        binaryExpr.setLeft(left);
        binaryExpr.setRight(right);
        binaryExpr.setOperator(OPERATOR.valueOf(ctx.operator.getText()));
        binarys.put(currentState.getName(),binaryExpr);
        System.out.println(binaryExpr);
        System.out.println(currentState.getName());

    }

    @Override public void enterUnaryexpr(ArduinomlParser.UnaryexprContext ctx) {
        UnaryExpr unary = new UnaryExpr();

        if( ctx.condition().sensorcondition()!=null){
            SensorCondition sensorCondition = new SensorCondition();
            sensorCondition.setSensor(sensors.get(ctx.condition().sensorcondition().trigger.getText()));
            sensorCondition.setValue(CONDITION.valueOf(ctx.condition().sensorcondition().value.getText()));
            unary.setCondition(sensorCondition);
        }
        else{
            TimeCondition timeCondition = new TimeCondition();
            timeCondition.setTime(Integer.parseInt(ctx.condition().timecondition().trigger.getText()));
            unary.setCondition(timeCondition);
        }


        unarys.put(currentState.getName(),unary);
        System.out.println(currentState.getName());

    }


}


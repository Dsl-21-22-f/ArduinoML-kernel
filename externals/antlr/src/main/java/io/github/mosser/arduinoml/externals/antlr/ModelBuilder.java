package io.github.mosser.arduinoml.externals.antlr;

import io.github.mosser.arduinoml.externals.antlr.grammar.*;


import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.*;
import io.github.mosser.arduinoml.kernel.structural.Actuator;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

import java.util.*;

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
    private List<TimeCondition>    timeConditions = new ArrayList<>();
    private List<SensorCondition>    sensorConditions  = new ArrayList<>();
    private Map<String, List<Binding>>  bindings  = new HashMap<>();



    private class Binding { // used to support state resolution for transitions
        String to; // name of the next state, as its instance might not have been compiled yet
        TimeCondition timeCondition;
        List<SensorCondition> sensorConditions;
    }



    private State currentState = null;
    private String currentNext = null;

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
        bindings.forEach((key, bindings) ->  {
            for(Binding b : bindings){
                Transition t = new Transition();
                t.setNext(states.get(b.to));
                t.setSensorConditions(b.sensorConditions);
                t.setTimeCondition(b.timeCondition);
                states.get(key).getTransitions().add(t);
            }

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
    public void enterAbstractTransition(ArduinomlParser.AbstractTransitionContext ctx) {
        // Creating a placeholder as the next state might not have been compiled yet.
        this.currentNext = ctx.next.getText();
    }
    @Override
    public void exitAbstractTransition(ArduinomlParser.AbstractTransitionContext ctx) {
        // Creating a placeholder as the next state might not have been compiled yet.

    }
    @Override
    public void exitTransition(ArduinomlParser.TransitionContext ctx) {
        Binding toBeResolvedLater = new Binding();
        toBeResolvedLater.to = this.currentNext;
        toBeResolvedLater.sensorConditions = new ArrayList<>(sensorConditions);
        if (!timeConditions.isEmpty())
            toBeResolvedLater.timeCondition = timeConditions.get(0);

        if (bindings.get(currentState.getName()) != null) {
            bindings.get(currentState.getName()).add(toBeResolvedLater);
        } else {
            List<Binding> list = new ArrayList<>();
            list.add(toBeResolvedLater);
            bindings.put(currentState.getName(), list);

        }
        sensorConditions.clear();
        timeConditions.clear();
    }
        @Override
    public void enterInitial(ArduinomlParser.InitialContext ctx) {
        this.theApp.setInitial(this.currentState);
    }


    @Override
    public void enterTimecondition(ArduinomlParser.TimeconditionContext ctx) {
        TimeCondition timeCondition = new TimeCondition();
        timeCondition.setTime(+Integer.parseInt(ctx.trigger.getText()));
        this.timeConditions.add(timeCondition);
    }

    @Override
    public void enterSensorcondition(ArduinomlParser.SensorconditionContext ctx) {
        SensorCondition sensorCondition = new SensorCondition();
        sensorCondition.setSensor(sensors.get(ctx.trigger.getText()));
        sensorCondition.setValue(CONDITION.valueOf(ctx.value.getText()));
        this.sensorConditions.add(sensorCondition);
    }

}


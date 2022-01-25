package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

public class SensorCondition extends AbstractCondition{

    private CONDITION value;

    private Sensor sensor;

    public CONDITION getValue() {
        return value;
    }


    public void setValue(CONDITION value) {
        this.value = value;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}

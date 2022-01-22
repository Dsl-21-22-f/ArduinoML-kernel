package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

public class UnaryExpr extends Expr{

    private CONDITION value;

    private Sensor sensor;

    private final ExprType type = ExprType.UNARY;



    @Override
    public ExprType getExprType() {
        return this.type;
    }

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


}

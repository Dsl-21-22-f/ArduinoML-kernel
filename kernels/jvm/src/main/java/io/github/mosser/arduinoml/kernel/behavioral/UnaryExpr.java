package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

public class UnaryExpr extends Expr{

    private SIGNAL value;

    private Sensor sensor;

    private final ExprType type = ExprType.UNARY;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExprType getExprType() {
        return this.type;
    }

    public SIGNAL getValue() {
        return value;
    }

    public void setValue(SIGNAL value) {
        this.value = value;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }


}

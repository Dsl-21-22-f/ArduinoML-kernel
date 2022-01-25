package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

public class UnaryExpr extends Expr{

    private AbstractCondition condition;

    public AbstractCondition getCondition() {
        return condition;
    }

    public void setCondition(AbstractCondition condition) {
        this.condition = condition;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String beforeExpr() {
        if( condition instanceof SensorCondition) {
            String sensorName = ((SensorCondition) this.condition).getSensor().getName();
            return String.format("\t\t\t%sBounceGuard = millis() - %sLastDebounceTime > debounce;\n",
                    sensorName, sensorName);
        }
        return "";
    }

    @Override
    public String afterExpr() {
        if( condition instanceof SensorCondition) {
            String sensorName = ((SensorCondition) this.condition).getSensor().getName();
            return String.format("\t\t\t\t%sLastDebounceTime = millis();\n", sensorName);
        }
        return "";    }
}

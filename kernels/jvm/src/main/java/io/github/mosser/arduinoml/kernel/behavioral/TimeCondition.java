package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;

public class TimeCondition extends  AbstractCondition{

    private int time;

    private final ConditionType type = ConditionType.TIME;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ConditionType getType() {
        return type;
    }
}

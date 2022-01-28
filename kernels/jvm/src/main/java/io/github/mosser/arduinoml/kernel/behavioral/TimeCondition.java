package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitable;
import io.github.mosser.arduinoml.kernel.generator.Visitor;

public class TimeCondition implements Visitable {

    private int time;

    private WHEN when;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public WHEN getWhen() {
        return when;
    }

    public void setWhen(WHEN when) {
        this.when = when;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}

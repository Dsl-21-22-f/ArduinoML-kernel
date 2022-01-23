package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitable;

public abstract class AbstractCondition implements Visitable {
    public abstract ConditionType getType();

}

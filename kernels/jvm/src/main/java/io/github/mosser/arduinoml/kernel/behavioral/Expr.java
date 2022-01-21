package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitable;

public abstract class Expr implements Visitable {

    public abstract ExprType getExprType();
}

package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

public class UnaryExpr extends Expr{

    private AbstractCondition condition;

    private final ExprType type = ExprType.UNARY;



    @Override
    public ExprType getExprType() {
        return this.type;
    }

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
}

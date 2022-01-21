package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;

public class BinaryExpr extends Expr {

    private OPERATOR operator;

    private UnaryExpr left;

    private UnaryExpr right;

    private final ExprType type = ExprType.BINARY;


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExprType getExprType() {
        return this.type;
    }

    public OPERATOR getOperator() {
        return operator;
    }

    public void setOperator(OPERATOR operator) {
        this.operator = operator;
    }

    public UnaryExpr getLeft() {
        return left;
    }

    public void setLeft(UnaryExpr left) {
        this.left = left;
    }

    public UnaryExpr getRight() {
        return right;
    }

    public void setRight(UnaryExpr right) {
        this.right = right;
    }
}

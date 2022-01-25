package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;

public class BinaryExpr extends Expr {

    private OPERATOR operator;

    private Expr left;

    private Expr right;



    public OPERATOR getOperator() {
        return operator;
    }

    public void setOperator(OPERATOR operator) {
        this.operator = operator;
    }

    public Expr getLeft() {
        return left;
    }

    public void setLeft(Expr left) {
        this.left = left;
    }

    public Expr getRight() {
        return right;
    }

    public void setRight(Expr right) {
        this.right = right;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String beforeExpr() {
        return left.beforeExpr() +right.beforeExpr();
    }

    @Override
    public String afterExpr() {
        return left.afterExpr() +right.afterExpr();
    }
}

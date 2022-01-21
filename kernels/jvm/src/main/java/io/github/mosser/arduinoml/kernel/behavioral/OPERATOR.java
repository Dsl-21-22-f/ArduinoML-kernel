package io.github.mosser.arduinoml.kernel.behavioral;

public enum OPERATOR {

    AND("&&"), OR("||");

    private String value;

    OPERATOR(String s) {
        this.value = s;
    }

    public String getValue(){
        return value;
    }
}

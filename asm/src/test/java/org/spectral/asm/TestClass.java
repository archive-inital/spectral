package org.spectral.asm;

public class TestClass implements MessagePrinter {

    private String message;

    TestClass() {
        setMessage("Hello World!");
        printMessage();
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void printMessage() {
        System.out.println("My message is: " + this.getMessage());
    }
}

package com.pnedelko;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Parser {
    private Scanner scanner;
    private String currentLine = "";
    private static final List<String> ARITHMETIC_COMMANDS = Arrays.asList(
        "add",
        "sub",
        "neg",
        "eq",
        "gt",
        "lt",
        "and",
        "or",
        "not"
    );

    Parser(File inputFile) throws FileNotFoundException {
        scanner = new Scanner(inputFile);
    }

    public boolean hasMoreCommands() {
        return scanner.hasNextLine();
    }

    public void advance() {
        String line = scanner.nextLine();
        String[] parts = line.split("//");
        if (parts.length > 0) {
            currentLine = parts[0].trim();
        } else {
            currentLine = "";
        }
    }

    public CommandType commandType() {
        if (currentLine.equals("")) {
            return CommandType.EMPTY;
        } else if (currentLine.startsWith("push")) {
            return CommandType.PUSH;
        } else if (currentLine.startsWith("pop")) {
            return CommandType.POP;
        } else if (currentLine.startsWith("label")) {
            return CommandType.LABEL;
        } else if (currentLine.startsWith("goto")) {
            return CommandType.GOTO;
        } else if (currentLine.startsWith("if-goto")) {
            return CommandType.IF;
        } else if (currentLine.startsWith("function")) {
            return CommandType.FUNCTION;
        } else if (currentLine.startsWith("return")) {
            return CommandType.RETURN;
        } else if (currentLine.startsWith("call")) {
            return CommandType.CALL;
        } else if (ARITHMETIC_COMMANDS.contains(currentLine)) {
            return CommandType.ARITHMETIC;
        }

        return CommandType.EMPTY; //@todo: throw some exception
    }

    public String getCommand() {
        return currentLine;
    }

    public String arg1() {
        if (commandType() == CommandType.ARITHMETIC) {
            return currentLine.split(" ")[0].trim();
        }

        return currentLine.split(" ")[1].trim();
    }

    public int arg2() {
        String secondPart = currentLine.split(" ")[2].trim();
        return Integer.parseInt(secondPart);
    }

    public void close() {
        this.scanner.close();
    }
}

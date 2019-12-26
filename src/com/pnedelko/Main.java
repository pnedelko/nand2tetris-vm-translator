package com.pnedelko;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("First argument has to be a filepath");
        }

        File inputFile = new File(args[0]);

        if (!inputFile.exists()) {
            System.err.println("Input file doesn't exist");
            return;
        }

        System.out.println("Input file: " + inputFile.getAbsolutePath());
        String dir = inputFile.getAbsoluteFile().getParent();

        File outputFile;

        if (!inputFile.getName().endsWith(".vm")) {
            // @todo: throw ???
            System.out.println("Filename should be .vm");
            return;
        }
        String inFileName = inputFile.getName();
        String outputFilename = inFileName.substring(0, inFileName.length() - 3) + ".asm";
        outputFile = new File(dir, outputFilename);

        System.out.println("Output file: " + outputFile.getAbsolutePath());

        CodeWriter codeWriter;

        try {
            codeWriter = new CodeWriter(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            Parser parser = new Parser(inputFile);

            while (parser.hasMoreCommands()) {
                parser.advance();
                if (parser.commandType() == CommandType.PUSH) {
//                    System.out.println("//"+parser.getCommand());
                    codeWriter.writePushPop(CommandType.PUSH, parser.arg1(), parser.arg2());
                } else if (parser.commandType() == CommandType.POP) {
//                    System.out.println("//"+parser.getCommand());
                    codeWriter.writePushPop(CommandType.POP, parser.arg1(), parser.arg2());
                } else if (parser.commandType() == CommandType.ARITHMETIC) {
//                    System.out.println("//"+parser.getCommand());
                    codeWriter.writeArithmetic(parser.arg1());
                }
            }
            parser.close();
            codeWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
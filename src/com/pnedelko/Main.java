package com.pnedelko;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class Main {
    private static CodeWriter codeWriter;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("First argument has to be a filepath");
        }

        File inputPath = new File(args[0]);

        if (!inputPath.exists()) {
            System.err.println("Input file doesn't exist");
            return;
        }

        System.out.println("Input path: " + inputPath.getAbsolutePath());

        if (inputPath.isDirectory()) {
            String dir = inputPath.getAbsolutePath();
            String inFileName = inputPath.getName();
            String outputFilename = inFileName + ".asm";
            File outputFile = new File(dir, outputFilename);
            System.out.println("Output file: " + outputFile.getAbsolutePath());

            try {
                codeWriter = new CodeWriter(outputFile);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            File[] files = inputPath.listFiles(pathname -> pathname.getName().endsWith(".vm"));

            assert files != null; //is it ok?

            codeWriter.writeInit();

            for (File file : files) {
                System.out.println("Processing file: " + file.getAbsolutePath());
                processInputFile(file);
            }
            codeWriter.close();
        } else {
            String dir = inputPath.getAbsoluteFile().getParent();
            if (!inputPath.getName().endsWith(".vm")) {
                // @todo: throw ???
                System.out.println("Filename should be .vm");
                return;
            }
            String inFileName = inputPath.getName();
            String outputFilename = inFileName.substring(0, inFileName.length() - 3) + ".asm";
            File outputFile = new File(dir, outputFilename);
            System.out.println("Output file: " + outputFile.getAbsolutePath());

            try {
                codeWriter = new CodeWriter(outputFile);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            codeWriter.writeInit();
            processInputFile(inputPath);
            codeWriter.close();
        }
    }

    private static void processInputFile(File inputFile) {
        try {
            Parser parser = new Parser(inputFile);
            codeWriter.setFilename(inputFile.getName());

            while (parser.hasMoreCommands()) {
                parser.advance();

                switch (parser.commandType()) {
                    case PUSH:
                        codeWriter.writePushPop(CommandType.PUSH, parser.arg1(), parser.arg2());
                        break;
                    case POP:
                        codeWriter.writePushPop(CommandType.POP, parser.arg1(), parser.arg2());
                        break;
                    case ARITHMETIC:
                        codeWriter.writeArithmetic(parser.arg1());
                        break;
                    case LABEL:
                        codeWriter.writeLabel(parser.arg1());
                        break;
                    case GOTO:
                        codeWriter.writeGoto(parser.arg1());
                        break;
                    case IF:
                        codeWriter.writeIf(parser.arg1());
                        break;
                    case CALL:
                        codeWriter.writeCall(parser.arg1(), parser.arg2());
                        break;
                    case RETURN:
                        codeWriter.writeReturn();
                        break;
                    case FUNCTION:
                        codeWriter.writeFunction(parser.arg1(), parser.arg2());
                        break;
                }
            }
            parser.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
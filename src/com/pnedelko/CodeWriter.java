package com.pnedelko;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter fileWriter;
    private int jumpIndex = 0;
    private String fileName;

    CodeWriter(File file) throws IOException {
        fileWriter = new PrintWriter(file);
        fileName = file.getName();
//        initSP();
    }

    public void setFilename(String fileName) {
        this.fileName = fileName;
    }

    public void writeArithmetic(String command) {
        fileWriter.println("//"+command);

        switch (command) {
            case "add":
                saveTopStackItemIntoD();
                saveTopStackItemAddressIntoA();
                fileWriter.println("M=D+M");
                break;
            case "sub":
                saveTopStackItemIntoD();
                saveTopStackItemAddressIntoA();
                fileWriter.println("M=M-D");
                break;
            case "eq":
                writeBooleanOperation("JEQ");
                break;
            case "lt":
                writeBooleanOperation("JLT");
                break;
            case "gt":
                writeBooleanOperation("JGT");
                break;
            case "neg":
                saveTopStackItemAddressIntoA();
                fileWriter.println("M=-M");
                break;
            case "and":
                saveTopStackItemIntoD();

                saveTopStackItemAddressIntoA();
                fileWriter.println("M=D&M");
                break;
            case "or":
                saveTopStackItemIntoD();
                saveTopStackItemAddressIntoA();
                fileWriter.println("M=D|M");
                break;
            case "not":
                saveTopStackItemAddressIntoA();
                fileWriter.println("M=!M");
                break;
            default:
                //@todo: throw exception
                System.err.println("Can't parse command: " + command);
        }

        incrementSP();
    }

    private void writeBooleanOperation(String op) {
        saveTopStackItemIntoD();

        saveTopStackItemAddressIntoA();
        fileWriter.println("D=M-D");

        fileWriter.println("@J"+jumpIndex);
        fileWriter.println("D;" + op);

        //condition not passed
        fileWriter.println("@SP");
        fileWriter.println("A=M");
        fileWriter.println("M=0");
        fileWriter.println("@J"+jumpIndex+"_END");
        fileWriter.println("0;JMP");

        //condition passed
        fileWriter.println("(J"+jumpIndex+")");
        fileWriter.println("@SP");
        fileWriter.println("A=M");
        fileWriter.println("M=-1");

        fileWriter.println("(J"+jumpIndex+"_END)");

        jumpIndex++;
    }

    public void writePushPop(CommandType commandType, String segment, int index) {
        fileWriter.println("//"+commandType + " " + segment + " " + index);

        if (commandType == CommandType.PUSH) {
            switch (segment) {
                case "constant":
                    saveStaticValueIntoD(index);
                    break;
                case "local":
                    saveSegmentValueIntoD("LCL", index);
                    break;
                case "argument":
                    saveSegmentValueIntoD("ARG", index);
                    break;
                case "this":
                    saveSegmentValueIntoD("THIS", index);
                    break;
                case "that":
                    saveSegmentValueIntoD("THAT", index);
                    break;
                case "static":
                    saveSegmentValueIntoD("STATIC", index);
                    break;
                case "temp":
                    saveSegmentValueIntoD("TEMP", index);
                    break;
                case "pointer":
                    saveSegmentValueIntoD("POINTER", index);
                    break;
                default:
                    System.err.println("Wrong segment name: " + segment);
            }

            fileWriter.println("@SP");
            fileWriter.println("A=M");
            fileWriter.println("M=D");
            fileWriter.println("@SP");
            fileWriter.println("M=M+1");
        } else if (commandType == CommandType.POP) {
            switch (segment) {
                case "local":
                    saveTopStackElementIntoSegment("LCL", index);
                    break;
                case "argument":
                    saveTopStackElementIntoSegment("ARG", index);
                    break;
                case "this":
                    saveTopStackElementIntoSegment("THIS", index);
                    break;
                case "that":
                    saveTopStackElementIntoSegment("THAT", index);
                    break;
                case "static":
                    saveTopStackElementIntoSegment("STATIC", index);
                    break;
                case "temp":
                    saveTopStackElementIntoSegment("TEMP", index);
                    break;
                case "pointer":
                    saveTopStackElementIntoSegment("POINTER", index);
                    break;
                default:
                    System.err.println("Wrong segment name: " + segment);
            }
        }
    }

    public void writeInit() {

    }

    public void writeLabel(String label) {
        fileWriter.println("//label "+label);
        fileWriter.println("("+label+")");
    }

    public void writeGoto(String label) {
        fileWriter.println("//goto "+label);
        fileWriter.println("@"+label);
        fileWriter.println("0;JMP");
    }

    public void writeIf(String label) {
        fileWriter.println("//if-goto "+label);
        saveTopStackItemIntoD();
        fileWriter.println("@"+label);
        fileWriter.println("D;JNE"); //jump if D not equal 0
    }

    public void writeCall(String functionName, int numArgs) {

    }

    public void writeReturn() {
        //endFrame = LCL
        fileWriter.println("@LCL");
        fileWriter.println("D=M");
        fileWriter.println("@endFrame");
        fileWriter.println("M=D");

        //retAddress = *(endFrame-5)
        fileWriter.println("@endFrame");
        fileWriter.println("D=M");
        fileWriter.println("@5");
        fileWriter.println("A=D-A");
        fileWriter.println("D=M");
        fileWriter.println("@retAddress");
        fileWriter.println("M=D");

        //*ARG = pop()
        saveTopStackItemIntoD();
        fileWriter.println("@ARG");
        fileWriter.println("A=M");
        fileWriter.println("M=D");

        //SP=ARG+1
        fileWriter.println("@ARG");
        fileWriter.println("D=M");
        fileWriter.println("@SP");
        fileWriter.println("M=D+1");

        //THAT = *(endFrame-1)
        fileWriter.println("@endFrame");
        fileWriter.println("D=M");
        fileWriter.println("@1");
        fileWriter.println("A=D-A");
        fileWriter.println("D=M");
        fileWriter.println("@THAT");
        fileWriter.println("M=D");

        //THIS = *(endFrame-2)
        fileWriter.println("@endFrame");
        fileWriter.println("D=M");
        fileWriter.println("@2");
        fileWriter.println("A=D-A");
        fileWriter.println("D=M");
        fileWriter.println("@THIS");
        fileWriter.println("M=D");

        //ARG = *(endFrame-3)
        fileWriter.println("@endFrame");
        fileWriter.println("D=M");
        fileWriter.println("@3");
        fileWriter.println("A=D-A");
        fileWriter.println("D=M");
        fileWriter.println("@ARG");
        fileWriter.println("M=D");

        //LCL = *(endFrame-4)
        fileWriter.println("@endFrame");
        fileWriter.println("D=M");
        fileWriter.println("@4");
        fileWriter.println("A=D-A");
        fileWriter.println("D=M");
        fileWriter.println("@LCL");
        fileWriter.println("M=D");

        //goto retAddress
        fileWriter.println("@retAddress");
        fileWriter.println("A=M");
        fileWriter.println("0;JMP");
    }

    public void writeFunction(String functionName, int numLocals) {
        fileWriter.println("//function " + functionName + " " + numLocals);
        fileWriter.println("("+functionName+")");
        for (int i = 0; i < numLocals; i++) {
            setLocalVariableToZero(i);
        }
    }

    private void initSP() {
        fileWriter.println("@256");
        fileWriter.println("D=A");
        fileWriter.println("@SP");
        fileWriter.println("M=D");
    }

    private void setLocalVariableToZero(int offset) {
        saveSegmentAddressIntoA("LCL", offset);
        fileWriter.println("M=0");
    }

    private void saveTopStackItemIntoD() {
        saveTopStackItemAddressIntoA();
        fileWriter.println("D=M");
    }

    private void saveTopStackItemAddressIntoA() {
        fileWriter.println("@SP");
        fileWriter.println("M=M-1");
        fileWriter.println("A=M");
    }

    private void incrementSP() {
        fileWriter.println("@SP");
        fileWriter.println("M=M+1");
    }

    private void saveStaticValueIntoD(int val) {
        fileWriter.println("@"+val);
        fileWriter.println("D=A");
    }

    private void saveSegmentValueIntoD(String segment, int offset) {
        saveSegmentAddressIntoA(segment, offset);
        fileWriter.println("D=M");
    }

    private void saveSegmentAddressIntoA(String segment, int offset) {
        switch (segment) {
            case "TEMP":
                saveStaticValueIntoD(offset);
                fileWriter.println("@5");
                fileWriter.println("A=A+D");
                break;
            case "POINTER":
                saveStaticValueIntoD(offset);
                fileWriter.println("@3");
                fileWriter.println("A=A+D");
                break;
            case "STATIC":
                fileWriter.println("@" + fileName + "." + offset);
                break;
            default:
                saveStaticValueIntoD(offset);
                fileWriter.println("@" + segment);
                fileWriter.println("A=M+D");
                break;
        }
    }

    private void saveTopStackElementIntoSegment(String segment, int offset) {
        saveTopStackItemIntoD();
        fileWriter.println("@R13");
        fileWriter.println("M=D"); //R13 = top stack value


        saveSegmentAddressIntoA(segment, offset);
        fileWriter.println("D=A"); //save address of segment into D

        fileWriter.println("@R14");
        fileWriter.println("M=D"); //R14 = address of segment

        fileWriter.println("@R13");
        fileWriter.println("D=M"); //D = value from stack

        fileWriter.println("@R14");
        fileWriter.println("A=M");
        fileWriter.println("M=D"); //write stack value by address stored in R14
    }

    public void close() {
        fileWriter.close();
    }
}

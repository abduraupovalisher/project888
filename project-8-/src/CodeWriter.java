import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {
    private String filename;
    private PrintWriter writeOut;
    private int labelNumber;

    private void out(String line) {
        writeOut.println(line);
    }

    private String nextLabel() {
        return String.valueOf(labelNumber++);
    }

    public CodeWriter(File output) throws FileNotFoundException {
        try {
            writeOut = new PrintWriter(output);
            labelNumber = 0;
        } catch (FileNotFoundException fnf) {
            throw new FileNotFoundException("File not found: " + fnf.getMessage());
        }
    }

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public void writeInit() {
        // SP ni 256 ga o'rnatish
        out("// initializing function");
        out("@256");
        out("D=A");
        out("@SP");
        out("M=D");

        // Kommentdan chiqarish kerak bo'lsa
        // writeCall("Sys.init", 0);
    }

    public void writeArithmetic(String operation) {
        out("// " + operation);

        switch (operation.toLowerCase()) {
            case "add":
            case "sub":
            case "and":
            case "or":
                out("@SP");
                out("AM=M-1");
                out("D=M");
                out("A=A-1");
                switch (operation.toLowerCase()){
                    case "add":
                        out("M=D+M");
                        break;
                    case "sub":
                        out("M=M-D");
                        break;
                    case "and":
                        out("M=M&D");
                        break;
                    case "or":
                        out("M=M|D");
                        break;
                }
                break;
            case "eq":
            case "lt":
            case "gt":
                String labelTrue = "COMPARE.TRUE." + nextLabel();
                String labelEnd = "COMPARE.END." + nextLabel();

                out("@SP");
                out("AM=M-1");
                out("D=M");
                out("A=A-1");
                out("D=M-D");

                switch (operation.toLowerCase()){
                    case "eq":
                        out("@" + labelTrue);
                        out("D;JEQ");
                        break;
                    case "lt":
                        out("@" + labelTrue);
                        out("D;JLT");
                        break;
                    case "gt":
                        out("@" + labelTrue);
                        out("D;JGT");
                        break;
                }

                out("@SP");
                out("A=M-1");
                out("M=0");
                out("@" + labelEnd);
                out("0;JMP");

                out("(" + labelTrue + ")");
                out("@SP");
                out("A=M-1");
                out("M=-1");

                out("(" + labelEnd + ")");
                break;
            case "not":
                out("@SP");
                out("A=M-1");
                out("M=!M");
                break;
            case "neg":
                out("@SP");
                out("A=M-1");
                out("M=-M");
                break;
        }
    }

    public void writePushPop(CommandType command, String segment, int index) {
        out("// " + command + " " + segment + " " + index);
        if (command == CommandType.C_PUSH) {
            switch (segment.toLowerCase()) {
                case "pointer":
                    out("@" + (index == 0 ? "THIS" : "THAT"));
                    out("D=M");
                    break;
                case "static":
                    out("@" + filename + "." + index);
                    out("D=M");
                    break;
                case "constant":
                    out("@" + index);
                    out("D=A");
                    break;
                case "temp":
                    out("@" + (5 + index));
                    out("D=M");
                    break;
                default:
                    out(getLabel(segment));
                    out("D=M");
                    out("@" + index);
                    out("A=D+A");
                    out("D=M");
                    break;
            }
            finishPush();
        } else if (command == CommandType.C_POP) {
            switch (segment.toLowerCase()) {
                case "pointer":
                    out("@" + (index == 0 ? "THIS" : "THAT"));
                    out("D=A");
                    break;
                case "static":
                    out("@" + filename + "." + index);
                    out("D=A");
                    break;
                case "temp":
                    out("@" + (5 + index));
                    out("D=A");
                    break;
                default:
                    out(getLabel(segment));
                    out("D=M");
                    out("@" + index);
                    out("D=D+A");
                    break;
            }
            out("@R13");
            out("M=D");
            out("@SP");
            out("AM=M-1");
            out("D=M");
            out("@R13");
            out("A=M");
            out("M=D");
        }
    }

    private void finishPush() {
        out("@SP");
        out("A=M");
        out("M=D");
        out("@SP");
        out("M=M+1");
    }

    public void writeLabel(String label) {
        out("// C_LABEL " + label);
        out("(" + label + ")");
    }

    public void writeGoto(String label) {
        out("// C_GOTO " + label);
        out("@" + label);
        out("0;JMP");
    }

    public void writeIf(String label) {
        out("// C_IF " + label);
        out("@SP");
        out("AM=M-1");
        out("D=M");
        out("@" + label);
        out("D;JNE");
    }

    public void writeCall(String functionName, int numArgs) {
        String label = nextLabel();
        out("// call " + functionName + " " + numArgs);

        out("@" + label + "_return");
        out("D=A");
        finishPush();

        out("@LCL");
        out("D=M");
        finishPush();

        out("@ARG");
        out("D=M");
        finishPush();

        out("@THIS");
        out("D=M");
        finishPush();

        out("@THAT");
        out("D=M");
        finishPush();

        out("@SP");
        out("D=M");
        out("@" + (numArgs + 5));
        out("D=D-A");
        out("@ARG");
        out("M=D");

        out("@SP");
        out("D=M");
        out("@LCL");
        out("M=D");

        out("@" + functionName);
        out("0;JMP");
        out("(" + label + "_return)");
    }

    public void writeReturn() {
        out("// return");

        out("@LCL");
        out("D=M");
        out("@R13");
        out("M=D");

        out("@5");
        out("A=D-A");
        out("D=M");
        out("@R14");
        out("M=D");

        out("@SP");
        out("AM=M-1");
        out("D=M");
        out("@ARG");
        out("A=M");
        out("M=D");

        out("@ARG");
        out("D=M+1");
        out("@SP");
        out("M=D");

        out("@R13");
        out("AM=M-1");
        out("D=M");
        out("@THAT");
        out("M=D");

        out("@R13");
        out("AM=M-1");
        out("D=M");
        out("@THIS");
        out("M=D");
        out("@R13");
        out("AM=M-1");
        out("D=M");
        out("@ARG");
        out("M=D");

        out("@R13");
        out("A=M-1");
        out("D=M");
        out("@LCL");
        out("M=D");

        out("@R14");
        out("A=M");
        out("0;JMP");
    }

    public void writeFunction(String functionName, int numLocals) {
        writeOut.println("// function " + functionName + " " + numLocals);
        out("(" + functionName + ")");
        for (int i = 0; i < numLocals; i++) {
            out("@0");
            out("D=A");
            finishPush();
        }
    }

    private String getLabel(String segment) {
        switch (segment.toLowerCase()) {
            case "local":
                return "@LCL";
            case "argument":
                return "@ARG";
            case "this":
                return "@THIS";
            case "that":
                return "@THAT";
            default:
                return null;
        }
    }

    public void close() {
        writeOut.close();
    }
}

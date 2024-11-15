import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class VMTranslator {
  private static final String ADD = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=M+D\n";
  private static final String SUB = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=M-D\n";
  private static final String NEG = "@SP\nM=M-1\nA=M\nM=-M\n";
  private static final String AND = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=M&D\n";
  private static final String OR = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=M|D\n";
  private static final String NOT = "@SP\nM=M-1\nA=M\nM=!M\n";
  private static int count = 0;
  private String nextCount() {
    count += 1;
    return Integer.toString(count);
  }

  private String EQ() {
    String n = nextCount();
    return "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\n@EQ_TRUE" + n + "\nD;JEQ\n@SP\nA=M\nM=0\n@EQ_END" + n + "\n0;JMP\n(EQ_TRUE" + n + ")\n@SP\nA=M\nM=-1\n(EQ_END" + n + ")\n";
  }

  private String GT() {
    String n = nextCount();
    return "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\n@GT_TRUE" + n + "\nD;JGT\n@SP\nA=M\nM=0\n@GT_END" + n + "\n0;JMP\n(GT_TRUE" + n + ")\n@SP\nA=M\nM=-1\n(GT_END" + n + ")\n";
  }

  private String LT() {
    String n = nextCount();
    return "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\n@LT_TRUE" + n + "\nD;JLT\n@SP\nA=M\nM=0\n@LT_END" + n + "\n0;JMP\n(LT_TRUE" + n + ")\n@SP\nA=M\nM=-1\n(LT_END" + n + ")\n";
  }

  private String parsePush(String base, String idx) throws Exception {
    String s = "";
    switch (base) {
      case "local": {
        s = "@LCL\nD=M\n@" + idx + "\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        break;
      }
      case "argument": {
        s = "@ARG\nD=M\n@" + idx + "\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        break;
      }
      case "this": {
        s = "@THIS\nD=M\n@" + idx + "\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        break;
      }
      case "that": {
        s = "@THAT\nD=M\n@" + idx + "\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        break;
      }
      case "constant": {
        s = "@" + idx + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        break;
      }
      case "static": {
        s = "@" + idx + "\nD=M\n@" + idx + "\nA=M\nM=D\n";
        break;
      }
      case "pointer": {
        if (idx.equals("0"))
          s = "@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        else
          s = "@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        break;
      }
      case "temp": {
        s = "@R5\nD=M\n@" + idx + "\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        break;
      }
      default: throw new Exception("bad command!");
    }
    return s;
  }

  private String parsePop(String base, String idx) throws Exception {
    String s = "";
    switch (base) {
      case "local": {
        s = "@LCL\nD=M\n@" + idx + "\nA=D+A\nD=A\n@R13\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
        break;
      }
      case "argument": {
        s = "@ARG\nD=M\n@" + idx + "\nA=D+A\nD=A\n@R13\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
        break;
      }
      case "this": {
        s = "@THIS\nD=M\n@" + idx + "\nA=D+A\nD=A\n@R13\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
        break;
      }
      case "that": {
        s = "@THAT\nD=M\n@" + idx + "\nA=D+A\nD=A\n@R13\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
        break;
      }
      case "pointer": {
        if (idx.equals("0"))
          s = "@THIS\nD=M\n@SP\nM=M-1\nA=M\nM=D\n";
        else
          s = "@THAT\nD=M\n@SP\nM=M-1\nA=M\nM=D\n";
        break;
      }
      case "temp": {
        s = "@R5\nD=M\n@" + idx + "\nA=D+A\nD=M\n@SP\nM=M-1\nA=M\nM=D\n";
        break;
      }
      case "static": {
        s = "@" + idx + "\nD=M\n@" + idx + "\nA=M\nM=D\n";
        break;
      }
      default: throw new Exception("bad command!");
    }
    return s;
  }

  // Flow control commands
  private String GOTO(String label) {
    return "@" + label + "\n0;JMP\n";
  }

  private String IFGOTO(String label) {
    return "@SP\nM=M-1\nA=M\nD=M\n@" + label + "\nD;JNE\n";
  }

  private String FUNCTION(String f, String k) {
    StringBuilder s = new StringBuilder("(" + f + ")\n");
    for (int i = 0; i < Integer.parseInt(k); i++) {
      s.append("@SP\nA=M\nM=0\n@SP\nM=M+1\n");
    }
    return s.toString();
  }

  private String CALL(String f, String n) {
    String c = nextCount();
    return "@SP\nD=M\n@R13\nM=D\n@" + f + "\n0;JMP\n(RET." + c + ")\n";
  }

  public static void main(String files[]) {
    if (files.length == 0) {
      System.err.println("no input files");
      return;
    }
    VMTranslator p = new VMTranslator();
    String init =
      "@256\n" +
      "D=A\n" +
      "@SP\n" +
      "M=D\n" +
      "// call Sys.init 0\n" +
      p.CALL("Sys.init", "0") +
      "0;JMP\n";
    System.out.println(init);
    String s;
    try {
      while(true) {
        s = p.parseNextCommand();
        if (s == null)
          return;
        System.out.println(s);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

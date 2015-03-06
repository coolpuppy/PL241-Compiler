package pl241.uci.edu.cfg;

import java.util.HashMap;

import pl241.uci.edu.ir.*;
import pl241.uci.edu.middleend.Instruction;
import pl241.uci.edu.middleend.InstructionType;
import pl241.uci.edu.optimizer.RegisterAllocation;
import pl241.uci.edu.frontend.Token;
import pl241.uci.edu.middleend.Result;

public class IntermediateCodeGenerator {
    public enum IOType{
        InputNum,OutputNum,OutputNewLine;
    }
    private RegisterAllocation registerAllocation;
    private HashMap<Token,InstructionType> OperatorOpMap;
    private HashMap<IOType,InstructionType> IOOpMap;

    public IntermediateCodeGenerator(){
        this.registerAllocation=new RegisterAllocation();
        this.OperatorOpMap=new HashMap<Token,InstructionType>();
        this.OperatorOpMap.put(Token.PLUS, InstructionType.ADD);
        this.OperatorOpMap.put(Token.MINUS, InstructionType.SUB);
        this.OperatorOpMap.put(Token.TIMES, InstructionType.MUL);
        this.OperatorOpMap.put(Token.DIVIDE, InstructionType.DIV);
        this.OperatorOpMap.put(Token.EQL, InstructionType.BNE);
        this.OperatorOpMap.put(Token.NEQ, InstructionType.BEQ);
        this.OperatorOpMap.put(Token.LSS, InstructionType.BGE);
        this.OperatorOpMap.put(Token.LEQ, InstructionType.BGT);
        this.OperatorOpMap.put(Token.GRE, InstructionType.BLE);
        this.OperatorOpMap.put(Token.GEQ, InstructionType.BLT);
        this.IOOpMap.put(IOType.InputNum, InstructionType.READ);
        this.IOOpMap.put(IOType.OutputNum, InstructionType.WRITE);
        this.IOOpMap.put(IOType.OutputNewLine, InstructionType.WLN);
    }

    public void generateArithmeticIC(BasicBlock curBlock,Token curToken,Result x, Result y){
        if(x.type== Result.ResultType.constant&&y.type== Result.ResultType.constant){
            switch(OperatorOpMap.get(curToken)){
                case ADD:
                    x.value=x.value+y.value;
                    break;
                case SUB:
                    x.value=x.value-y.value;
                    break;
                case MUL:
                    x.value=x.value*y.value;
                    break;
                case DIV:
                    x.value=x.value/y.value;
                    break;
            }
        }
        else{
            //if y is not constant, deallocate y from registers.
            if(y.type==Result.ResultType.constant){
                curBlock.generateInstruction(OperatorOpMap.get(curToken),x,y);
            }
            else{
                curBlock.generateInstruction(OperatorOpMap.get(curToken),x,y);
                //TODO: DEALLOCATE Y
            }
        }
    }

    public void generateCMPIC(BasicBlock curBlock,Result x,Result y){
        curBlock.generateInstruction(InstructionType.CMP,x,y);
    }

    public void generateIOIC(BasicBlock curBlock,IOType ioType,Result x){
        curBlock.generateInstruction(IOOpMap.get(ioType),x,null);
    }

    public void generateVarDeclIC(BasicBlock curBlock,Result x,FunctionDecl function){
        int varIdent=x.varIdent;
        x.setSSAVersion(Instruction.getPc());
        VariableTable.addSSAUseChain(x.varIdent,x.ssaVersion);
        if(function!=null){
            function.addLocalVariable(x.varIdent);
        }
        else{
            VariableTable.addGlobalVariable(x.varIdent);
        }
        Result zeroConstant=Result.buildConstant(0);
        curBlock.generateInstruction(InstructionType.MOVE,zeroConstant,x);
    }

    public void assignmentIC(BasicBlock curBlock,BasicBlock joinBlock,Result variable,Result value){
        variable.setSSAVersion(Instruction.getPc());
        VariableTable.addSSAUseChain(variable.varIdent,variable.ssaVersion);
        curBlock.generateInstruction(InstructionType.MOVE,value,variable);
        if(joinBlock!=null){
            joinBlock.updatePhiFunction(variable.varIdent,variable.ssaVersion,curBlock.getType());
        }
    }

    public void returnStateIC(BasicBlock curBlock,Result variable,FunctionDecl function){
        Result returnInstr=new Result();
        returnInstr.buildResult(Result.ResultType.instruction,Instruction.getPc());
        curBlock.generateInstruction(InstructionType.MOVE,variable,returnInstr);
        function.setReturnInstr(returnInstr);
    }

}

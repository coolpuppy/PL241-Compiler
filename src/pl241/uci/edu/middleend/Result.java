package pl241.uci.edu.middleend;

import pl241.uci.edu.frontend.Scanner;
/*
Data:2015/03/02
This is for the intermediate representation and store the variable in pre-SSA form.
 */
public class Result {
    public enum Type{
        constant, variable, register, condition, branch, instruction;
    }
    public Type type;//result type
    public int value;//value if constant
    public int varAddress;//address if variable
    public SSAValue ssaVersion;//ssa version if variable
    public int regno;//register number if register
    public int fixuplocation;
    public BasicBlock branchBlock;
    public int instrRef;

    public Result(){

    }

    public Result(Result result){
        this.type=result.type;
        this.value=result.value;
        this.varAddress=result.varAddress;
        this.ssaVersion=result.ssaVersion;
        this.regno=result.regno;
        this.fixuplocation=result.fixuplocation;
        this.branchBlock=result.branchBlock;
        this.instrRef=result.instrRef;
    }

    public void buildResult(Type type,int inputValue){
        switch(type) {
            case constant:
                this.type = type;
                this.value = inputValue;
                break;
            case variable:
                this.type = type;
                this.varAddress = inputValue;
                break;
            case register:
                this.type = type;
                this.regno = inputValue;
                break;
            case instruction:
                this.type = type;
                this.instrRef = inputValue;
                break;
            default:
                break;
        }
    }

    public void setSSAVersion(SSAValue inputSSAVersion){
        this.ssaVersion=inputSSAVersion;
    }

    public Result buildConstant(int value){
        Result result=new Result();
        result.buildResult(Type.constant,value);
        return result;
    }

    public Result buildBranch(BasicBlock branchBlock){
        Result result=new Result();
        result.type=Type.branch;
        result.branchBlock=branchBlock;
        return result;
    }
}

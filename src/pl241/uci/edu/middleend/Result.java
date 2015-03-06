package pl241.uci.edu.middleend;

import pl241.uci.edu.frontend.Scanner;
import pl241.uci.edu.ir.BasicBlock;
import pl241.uci.edu.frontend.Token;

/*
Data:2015/03/02
This is for the intermediate representation and store the variable in pre-SSA form.
 */
public class Result {
    public enum ResultType{
        constant, variable, register, condition, branch, instruction;
    }
    public ResultType type;//result type
    public int value;//value if constant
    public int varIdent;//address if variable
    public SSAValue ssaVersion;//ssa version if variable
    public int regno;//register number if register
    public int fixuplocation;
    public Token relOp;
    public BasicBlock branchBlock;
    public int instrRef;

    public Result(){

    }

    public Result(Result result){
        this.type=result.type;
        this.value=result.value;
        this.varIdent=result.varIdent;
        this.ssaVersion=result.ssaVersion;
        this.regno=result.regno;
        this.fixuplocation=result.fixuplocation;
        this.relOp=result.relOp;
        this.branchBlock=result.branchBlock;
        this.instrRef=result.instrRef;
    }

    public void buildResult(ResultType type,int inputValue){
        switch(type) {
            case constant:
                this.type = type;
                this.value = inputValue;
                break;
            case variable:
                this.type = type;
                this.varIdent = inputValue;
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

    public void setSSAVersion(int inputSSAVersion){
        this.ssaVersion=new SSAValue(inputSSAVersion);
    }

    public Result buildConstant(int value){
        Result result=new Result();
        result.buildResult(ResultType.constant,value);
        return result;
    }

    public Result buildBranch(BasicBlock branchBlock){
        Result result=new Result();
        result.type=ResultType.branch;
        result.branchBlock=branchBlock;
        return result;
    }

    public Result deepClone(Result r)
    {
        return new Result(r);
    }
}

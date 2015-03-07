package pl241.uci.edu.frontend;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import pl241.uci.edu.cfg.ControlFlowGraph;
import pl241.uci.edu.cfg.DelUseChain;
import pl241.uci.edu.backend.IRCodeGenerator;
import pl241.uci.edu.cfg.VariableTable;
import pl241.uci.edu.ir.BasicBlock;
import pl241.uci.edu.middleend.Instruction;
import pl241.uci.edu.middleend.Result;

/*
Date:2015/01/26
This class is used to do the syntax analysis of the program.
 */

/*
Implementation of parser based on the EBNF of PL241:
    letter = “a” | “b” | … | “z”.
    digit = “0” | “1” | … | “9”.
    relOp = “==“ | “!=“ | “<“ | “<=“ | “>“ | “>=“.
    ident = letter {letter | digit}.
    number = digit {digit}.
    designator = ident{ "[" expression "]" }.
    factor = designator | number | “(“ expression “)” | funcCall .
    term = factor { (“*” | “/”) factor}.
    expression = term {(“+” | “-”) term}.
    relation = expression relOp expression .
    assignment = “let” designator “<-” expression.
    funcCall = “call” ident [ “(“ [expression { “,” expression } ] “)” ].
    ifStatement = “if” relation “then” statSequence [ “else” statSequence ] “fi”.
    whileStatement = “while” relation “do” StatSequence “od”.
    returnStatement = “return” [ expression ] .
    statement = assignment | funcCall | ifStatement | whileStatement | returnStatement.
    statSequence = statement { “;” statement }.
    typeDecl = “var” | “array” “[“ number “]” { “[“ number “]” }.
    varDecl = typeDecl ident { “,” ident } “;” .
    funcDecl = (“function” | “procedure”) ident [formalParam] “;” funcBody “;” .
    formalParam = “(“ [ident { “,” ident }] “)” .
    funcBody = { varDecl } “{” [ statSequence ] “}”.
    computation = “main” { varDecl } { funcDecl } “{” statSequence “}” “.” .
 */

public class Parser {
    private Scanner scanner;

    private Token curToken;

    private IRCodeGenerator irCodeGenerator;

    public Parser (String path)throws IOException{
        scanner=new Scanner(path);
        irCodeGenerator=new IRCodeGenerator();
        new ControlFlowGraph();
    }

    //designator = ident{ "[" expression "]" }.
    private Result designator(BasicBlock curBlock) throws IOException,Error{
        Result designator=new Result();
        List<Result> dimensions=new ArrayList<Result>();
        if(curToken==Token.IDENTIFIER){
            designator.buildResult(Result.ResultType.variable,scanner.getVarIdent());
            Next();
            while(curToken==Token.OPEN_BRACKET){
                Next();
                dimensions.add(expression(curBlock));
                if(curToken==Token.CLOSE_BRACKET){
                    Next();
                }
                else{
                    Error("designator expect ']'");
                }
            }
        }
        else{
            Error("designator expect an identifier");
        }
        if(dimensions.isEmpty()){
            return designator;
        }
        else{
            return designator;
        }
    }

    //factor = designator | number | “(“ expression “)” | funcCall .
    private Result factor(BasicBlock curBlock) throws IOException,Error{
        Result factor=new Result();
        if(curToken==Token.IDENTIFIER){
            factor=designator(curBlock);
            factor.ssaVersion= VariableTable.getLatestVersion(factor.varIdent);
        }
        else if(curToken==Token.NUMBER){
            factor.buildResult(Result.ResultType.constant,scanner.getVal());
            Next();
        }
        else if(curToken==Token.OPEN_PARENTHESIS){
            Next();
            factor=expression(curBlock);
            if(curToken==Token.CLOSE_PARENTHESIS){
                Next();
            }
            else{
                Error("factor expect ')'");
            }
        }
        else if(curToken==Token.CALL){
            factor=funcCall(curBlock);

        }
        else{
            Error("invalid factor");
        }
        return factor;
    }

    //term = factor { (“*” | “/”) factor}.
    private Result term(BasicBlock curBlock) throws IOException,Error{
        Result x=factor(curBlock);
        while(curToken==Token.TIMES||curToken==Token.DIVIDE){
            Token operator=curToken;
            Next();
            Result y=factor(curBlock);
            x.instrRef= Instruction.getPc();
            ControlFlowGraph.delUseChain.updateDefUseChain(x,y);
            irCodeGenerator.generateArithmeticIC(curBlock,operator,x,y);
        }
        return x;
    }

    //expression = term {(“+” | “-”) term}.
    private Result expression(BasicBlock curBlock) throws IOException,Error{
        Result x=term(curBlock);
        while(curToken==Token.PLUS||curToken==Token.MINUS){
            Token operator=curToken;
            Next();
            Result y=term(curBlock);
            x.instrRef=Instruction.getPc();
            ControlFlowGraph.delUseChain.updateDefUseChain(x,y);
            irCodeGenerator.generateArithmeticIC(curBlock,operator,x,y);
        }
        return x;
    }

    //relation = expression relOp expression .
    private Result relation(BasicBlock curBlock) throws IOException,Error{
        Result relation=null;
        Result x=expression(curBlock);
        if(curToken==Token.EQL||curToken==Token.NEQ||curToken==Token.LEQ||curToken==Token.LSS||curToken==Token.GEQ||curToken==Token.GRE){
            Token relOp=curToken;
            Next();
            Result y=expression(curBlock);
            irCodeGenerator.generateCMPIC(curBlock,x,y);
            relation=new Result();
            relation.relOp=relOp;
            relation.type= Result.ResultType.condition;
            relation.fixuplocation=0;
        }
        else{
            Error("invalid relation expect a relation operator");
        }
        return relation;
    }

    private Result assignment(){

    }

    private Result funcCall(){return null;}

    private BasicBlock ifStatement(){return null;}

    private BasicBlock whileStatement(){return null;}

    private BasicBlock statement(){return null;}

    private BasicBlock stateSequence(){return null;}

    private Result typeDecl(){return null;}

    private Result varDecl(){return null;}

    private Result funcDecl(){return null;}

    private Result formalParam(){return null;}

    private Result funcBody(){return null;}

    public void computation(){}

    private void Next() throws IOException{
        curToken=scanner.getNextToken();
    }

    private void Error(String msg)
    {
        System.out.println("Parser Error! " + msg);
    }
}

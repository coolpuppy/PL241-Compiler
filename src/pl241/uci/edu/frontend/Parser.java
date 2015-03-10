package pl241.uci.edu.frontend;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import pl241.uci.edu.cfg.ControlFlowGraph;
import pl241.uci.edu.cfg.DelUseChain;
import pl241.uci.edu.backend.IRCodeGenerator;
import pl241.uci.edu.cfg.VariableTable;
import pl241.uci.edu.ir.BasicBlock;
import pl241.uci.edu.ir.FunctionDecl;
import pl241.uci.edu.middleend.Instruction;
import pl241.uci.edu.middleend.InstructionType;
import pl241.uci.edu.middleend.Result;
import sun.org.mozilla.javascript.internal.Function;

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
        new VariableTable();
    }

    public void parser() throws Throwable
    {
        scanner.startScanner();
        Next();
        computation();
        scanner.closeScanner();
    }

    //designator = ident{ "[" expression "]" }.
    private Result designator(BasicBlock curBlock,Stack<BasicBlock>joinBlocks) throws IOException,Error{
        Result designator=new Result();
        List<Result> dimensions=new ArrayList<Result>();
        if(curToken==Token.IDENTIFIER){
            designator.buildResult(Result.ResultType.variable,scanner.getVarIdent());
            Next();
            while(curToken==Token.OPEN_BRACKET){
                Next();
                dimensions.add(expression(curBlock,joinBlocks));
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
    private Result factor(BasicBlock curBlock,Stack<BasicBlock>joinBlocks) throws IOException,Error{
        Result factor=new Result();
        if(curToken==Token.IDENTIFIER){
            factor=designator(curBlock,joinBlocks);
            factor.ssaVersion= VariableTable.getLatestVersion(factor.varIdent);
        }
        else if(curToken==Token.NUMBER){
            factor.buildResult(Result.ResultType.constant,scanner.getVal());
            Next();
        }
        else if(curToken==Token.OPEN_PARENTHESIS){
            Next();
            factor=expression(curBlock,joinBlocks);
            if(curToken==Token.CLOSE_PARENTHESIS){
                Next();
            }
            else{
                Error("factor expect ')'");
            }
        }
        else if(curToken==Token.CALL){
            factor=funcCall(curBlock,joinBlocks);
        }
        else{
            Error("invalid factor");
        }
        return factor;
    }

    //term = factor { (“*” | “/”) factor}.
    private Result term(BasicBlock curBlock,Stack<BasicBlock>joinBlocks) throws IOException,Error{
        Result x=factor(curBlock,joinBlocks);
        while(curToken==Token.TIMES||curToken==Token.DIVIDE){
            Token operator=curToken;
            Next();
            Result y=factor(curBlock,joinBlocks);
            x.instrRef= Instruction.getPc();
            ControlFlowGraph.delUseChain.updateDefUseChain(x,y);
            irCodeGenerator.generateArithmeticIC(curBlock,operator,x,y);
        }
        return x;
    }

    //expression = term {(“+” | “-”) term}.
    private Result expression(BasicBlock curBlock,Stack<BasicBlock>joinBlocks) throws IOException,Error{
        Result x=term(curBlock,joinBlocks);
        while(curToken==Token.PLUS||curToken==Token.MINUS){
            Token operator=curToken;
            Next();
            Result y=term(curBlock,joinBlocks);
            x.instrRef=Instruction.getPc();
            ControlFlowGraph.delUseChain.updateDefUseChain(x,y);
            irCodeGenerator.generateArithmeticIC(curBlock,operator,x,y);
        }
        return x;
    }

    //relation = expression relOp expression .
    private Result relation(BasicBlock curBlock,Stack<BasicBlock>joinBlocks) throws IOException,Error{
        Result relation=null;
        Result x=expression(curBlock,joinBlocks);
        if(curToken==Token.EQL||curToken==Token.NEQ||curToken==Token.LEQ||curToken==Token.LSS||curToken==Token.GEQ||curToken==Token.GRE){
            Token relOp=curToken;
            Next();
            Result y=expression(curBlock,joinBlocks);
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

    //assignment = “let” designator “<-” expression.
    private void assignment(BasicBlock curBlock,Stack<BasicBlock> joinBlocks) throws IOException,Error{
        if(curToken==Token.LET){
            Next();
            Result variable=designator(curBlock,joinBlocks);
            if(joinBlocks!=null&&joinBlocks.size()>0){
                joinBlocks.peek().createPhiFunction(variable.varIdent);
            }
            if(curToken==Token.BECOMETO){
                Next();
                Result value=expression(curBlock,joinBlocks);
                if(joinBlocks!=null){
                    irCodeGenerator.assignmentIC(curBlock,joinBlocks.peek(),variable,value);
                }
                else{
                    irCodeGenerator.assignmentIC(curBlock,null,variable,value);
                }
            }
            else{
                Error("wrong assignment, expect '<-'!");
            }
        }
        else{
            Error("wrong assignment, expect 'let'!");
        }
    }

    //funcCall = “call” ident [ “(“ [expression { “,” expression } ] “)” ].
    private Result funcCall(BasicBlock curBlock,Stack<BasicBlock>joinBlocks)throws IOException{
        Result x = new Result();
        FunctionDecl func = null;
        if(curToken == Token.CALL)
        {
            Next();
            if(curToken == Token.IDENTIFIER) {
                //x is a function variable in funcCall
                x.buildResult(Result.ResultType.variable, scanner.getID());

                func = ControlFlowGraph.allFunctions.get(x.varIdent);
                Next();

                //index for the number of expression
                int index = 0;

                if (curToken == Token.OPEN_PARENTHESIS) {
                    Next();

                    //find expression
                    if (isExpression(curToken)) {
                        Result y = expression(curBlock, joinBlocks);

                        //get the original definition of y
                        //don't need to do this for the definition of function
                        if (x.varIdent >= 3)
                            irCodeGenerator.generateASSIGNMENTIC(curBlock, y, func.getParameters().get(index++));

                        while (curToken == Token.COMMA) {
                            Next();
                            y = expression(curBlock, joinBlocks);
                            if (x.varIdent >= 3)
                                irCodeGenerator.generateASSIGNMENTIC(curBlock, y, func.getParameters().get(index++));
                        }
                    }

                    if (curToken == Token.CLOSE_PARENTHESIS)
                        Next();
                    else
                        Error("Expect ) in funcCall!");

                    //branch to function
                    if (x.varIdent == 0) {
                        //need to read
                        Instruction ins = irCodeGenerator.generateIOIC(curBlock, x.varIdent, null);
                        Result returnRE = new Result();
                        returnRE.buildResult(Result.ResultType.instruction, ins.getInstructionPC());
                        return returnRE;
                    } else if (x.varIdent < 3) {
                        irCodeGenerator.generateIOIC(curBlock, x.varIdent, null);
                    } else {
                        Result branch = Result.buildBranch(ControlFlowGraph.allFunctions.get(x.varIdent).getFirstFuncBlock());
                    }
                }
                else
                {
                    //TODO:WHY NEED THIS? A FUNCTION WITHOUT PARAMETER?
                    //branch to function
                    if (x.varIdent == 0) {
                        //need to read
                        Instruction ins = irCodeGenerator.generateIOIC(curBlock, x.varIdent, null);
                        Result returnRE = new Result();
                        returnRE.buildResult(Result.ResultType.instruction, ins.getInstructionPC());
                        return returnRE;
                    } else if (x.varIdent < 3) {
                        irCodeGenerator.generateIOIC(curBlock, x.varIdent, null);
                    } else {
                        Result branch = Result.buildBranch(ControlFlowGraph.allFunctions.get(x.varIdent).getFirstFuncBlock());
                    }
                }
            }
            else
                Error("Expect IDENT in funcCall");
        }
        else
            Error("Expect CALL token in funcCall!");
        return x.varIdent<3 ? null:func.getReturnInstr();
    }

    //ifStatement = “if” relation “then” statSequence [ “else” statSequence ] “fi”.
    private BasicBlock ifStatement(BasicBlock curBlock,Stack<BasicBlock> joinBlocks){
        return null;
    }

    private BasicBlock whileStatement(){return null;}

    //returnStatement = “return” [ expression ] .
    private Result returnStatement(BasicBlock curBlock,Stack<BasicBlock>joinBlocks) throws IOException{
        Result x = new Result();
        if(curToken == Token.RETURN)
        {
            Next();
            if(isExpression(curToken))
                x = expression(curBlock,joinBlocks);
        }
        else
            Error("Expect RETURN in returnStatement!");
        return x;
    }

    //statement = assignment | funcCall | ifStatement | whileStatement | returnStatement.
    private BasicBlock statement(BasicBlock curBlock,Stack<BasicBlock> joinBlocks,FunctionDecl function) throws IOException,Error{
        if(curToken==Token.LET){
            assignment(curBlock,joinBlocks);
            return curBlock;
        }
        else if(curToken == Token.CALL)
        {
            funcCall(curBlock,joinBlocks);
            return curBlock;
        }
        else if(curToken == Token.IF)
        {
            return ifStatement(curBlock,joinBlocks);
        }
        else if(curToken == Token.WHILE)
        {
            return whileStatement();
        }
        else if(curToken == Token.RETURN)
        {
            Result x = returnStatement(curBlock,joinBlocks);
            irCodeGenerator.generateReturnOp(curBlock,x,function);
            return curBlock;
        }
        else{
            Error("invalid statement!");
            return null;
        }
    }

    //statSequence = statement { “;” statement }.
    private BasicBlock stateSequence(BasicBlock curBlock,Stack<BasicBlock> joinBlocks,FunctionDecl function) throws IOException,Error{
        BasicBlock nextBlock=statement(curBlock,joinBlocks,function);
        if(curToken==Token.SEMICOMA){
            Next();
            nextBlock=statement(nextBlock,joinBlocks,function);
        }
        return nextBlock;
    }

    //typeDecl = “var” | “array” “[“ number “]” { “[“ number “]” }.
    private void typeDecl() throws IOException{
        if(curToken == Token.VAR)
            Next();
        else if(curToken == Token.ARRAY)
        {
            Next();
            boolean hasDimension = false;
            while(curToken == Token.OPEN_BRACKET)
            {
                hasDimension = true;
                Next();
                Result x = new Result();
                if(curToken == Token.NUMBER)
                {
                    x.buildResult(Result.ResultType.constant,scanner.getVal());
                    Next();
                }
                else
                {
                    Error("Expect number in the dimension of ARRAY declaration!");
                }

                if(curToken == Token.CLOSE_BRACKET)
                    Next();
                else
                    Error("Expect ] in ARRAY declaration!");
            }

            if(!hasDimension)
                Error("Expect [] in ARRAY declaration!");
        }
        else
        {
            Error("Expect VAR or ARRAY in typeDecl!");
        }
    }

    //varDecl = typeDecl ident { “,” ident } “;” .
    private void varDecl(BasicBlock curBlock,FunctionDecl function) throws IOException{
        Result x = null;

        //call the typedel for the variable and array
        typeDecl();

        if(this.curToken == Token.IDENTIFIER)
        {
            x = new Result();
            x.buildResult(Result.ResultType.variable,scanner.getID());

            //declare the variable
            declareVariable(curBlock,x,function);
            Next();
            while(curToken == Token.COMMA)
            {
                Next();
                if(this.curToken == Token.IDENTIFIER) {
                    x = new Result();
                    x.buildResult(Result.ResultType.variable, scanner.getID());

                    //declare the variable
                    declareVariable(curBlock, x, function);
                    Next();
                }
                else
                {
                    Error("expect IDENT in variable declaration!");
                }
            }
            if(curToken == Token.SEMICOMA)
            {
                Next();
            }
            else
            {
                Error("Expect ;, in the end of variable declaration!");
            }
        }
        else
        {
            Error("expect IDENT in variable declaration!");
        }
    }

    private void declareVariable(BasicBlock curBlock,Result x, FunctionDecl function)
    {
        if(x.type != Result.ResultType.variable)
            this.Error("Variable declaration error! type should be variable! in " + scanner.getLineNumber());
        else
        {
            int varIdent = x.varIdent;
            x.setSSAVersion(Instruction.getPc());
            VariableTable.addSSAUseChain(x.varIdent,x.ssaVersion);

            if(function!=null)
            {
                function.getLocalVariables().add(varIdent);
            }
            else
            {
                VariableTable.addGlobalVariable(varIdent);
            }

            //we add a move at the end of every variable declaration
            curBlock.generateInstruction(InstructionType.MOVE, Result.buildConstant(0), x);
        }
    }

    //funcDecl = (“function” | “procedure”) ident [formalParam] “;” funcBody “;” .
    private void funcDecl() throws IOException{
        if(curToken == Token.FUNCTION || curToken == Token.PROCEDURE) {
            Next();
            if(curToken == Token.IDENTIFIER)
            {
                Result x = new Result();
                x.buildResult(Result.ResultType.variable,scanner.getID());
                //declare function
                FunctionDecl function = declareFunction(x);
                Next();
                if(curToken == Token.OPEN_PARENTHESIS)
                    formalParam(function);
                if(curToken == Token.SEMICOMA)
                {
                    Next();
                    funcBody(function);
                    if (curToken == Token.SEMICOMA)
                        Next();
                    else
                        Error("Expect ; after funcBody in funcDecl");
                }
                else
                    Error("Expect ; after IDENT in funcDecl");
            }
            else
                Error("Expect IDENT in funcDecl!");
        }
        else
            Error("Expect function or procedure name in funcDecl!");
    }

    private FunctionDecl declareFunction(Result x)
    {
        if(x.type != Result.ResultType.variable)
            Error("declareFunction error! type of x should be variable");
        else
        {
            int functionIdent = x.varIdent;

            if(!ControlFlowGraph.allFunctions.containsKey(functionIdent))
            {
                FunctionDecl func = new FunctionDecl(functionIdent);
                ControlFlowGraph.allFunctions.put(functionIdent,func);
                return func;
            }
            else
            {
                Error("Function name redefined!");
                return null;
            }
        }
        return null;
    }

    //formalParam = “(“ [ident { “,” ident }] “)” .
    private void formalParam(FunctionDecl function) throws IOException{
        Result x = null;
        if(curToken == Token.OPEN_PARENTHESIS)
        {
            Next();
            if(curToken == Token.IDENTIFIER)
            {
                x = new Result();
                x.buildResult(Result.ResultType.variable,scanner.getID());
                //declare variable
                declareVariable(function.getFirstFuncBlock(),x,function);
                //add variable to parameters
                function.getParameters().add(x);
                Next();
                while(curToken == Token.COMMA)
                {
                    Next();
                    if(curToken == Token.IDENTIFIER) {
                        x = new Result();
                        x.buildResult(Result.ResultType.variable, scanner.getID());
                        //declare variable
                        declareVariable(function.getFirstFuncBlock(), x, function);
                        //add variable to parameters
                        function.getParameters().add(x);
                        Next();
                    }
                    else
                        Error("Expect ident in formalParam!");
                }
            }
            else
                Error("Expect ident in formalParam!");
            if(curToken == Token.CLOSE_PARENTHESIS)
                Next();
            else
                Error("Expect ) in formalParam!");
        }
        else
            Error("Expect ( in formalParam!");
    }

    //funcBody = { varDecl } “{” [ statSequence ] “}”.
    private void funcBody(FunctionDecl function) throws IOException {
        while(curToken == Token.VAR || curToken == Token.ARRAY)
            varDecl(function.getFirstFuncBlock(),function);

        if(curToken == Token.OPEN_BRACE)
        {
            Next();

            if(isStatement(curToken))
                stateSequence(function.getFirstFuncBlock(),null,function);

            if (curToken == Token.CLOSE_BRACE)
                Next();
            else
                Error("Expect } in funcBody!");
        }
        else
            Error("Expect { in funcBody!");
    }

    //computation = “main” { varDecl } { funcDecl } “{” statSequence “}” “.” .
    private void computation() throws IOException{
        if(curToken == Token.MAIN)
        {
            Next();

            while(curToken == Token.VAR || curToken == Token.ARRAY)
                varDecl(ControlFlowGraph.getFirstBlock(),null);

            while(curToken == Token.FUNCTION || curToken == Token.PROCEDURE)
                funcDecl();
            if(curToken == Token.OPEN_BRACE){
                Next();
                BasicBlock lastBlock = stateSequence(ControlFlowGraph.getFirstBlock(),null,null);
                if(curToken == Token.CLOSE_BRACE)
                {
                    Next();
                    if(curToken == Token.PERIOD)
                    {
                        Next();
                        lastBlock.generateInstruction(InstructionType.END,null,null);
                    }
                    else
                        Error("Expect . in the end!");
                }
                else
                    Error("Expect } in computation!");
            }
            else
                Error("Expect { in computation!");
        }
        else
            Error("Expect MAIN in computation!");
    }

    private void Next() throws IOException{
        curToken=scanner.getNextToken();
    }

    private void Error(String msg)
    {
        System.out.println("Parser Error! " + msg);
    }

    private boolean isStatement(Token token)
    {
        return (token == Token.IF || token == Token.WHILE || token == Token.CALL || token == Token.RETURN || token == Token.LET);
    }

    private boolean isExpression(Token token)
    {
        return (token == Token.IDENTIFIER || token == Token.NUMBER || token == Token.OPEN_PARENTHESIS || token == Token.CALL);
    }

    public static void main(String []args) throws Throwable{

        Parser p = new Parser("src/test/test001.txt");
        p.parser();
        ControlFlowGraph.printInstruction();
    }
}

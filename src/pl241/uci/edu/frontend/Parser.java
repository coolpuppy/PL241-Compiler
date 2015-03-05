package pl241.uci.edu.frontend;

import pl241.uci.edu.ir.BasicBlock;
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


    private Result designator(){return null;}

    private Result factor(){return null;}

    private Result term(){return null;}

    private Result expression(){return null;}

    private Result relation(){return null;}

    private Result assignment(){return null;}

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


    private void Error(String msg)
    {
        System.out.println("Parser Error! " + msg);
    }
}

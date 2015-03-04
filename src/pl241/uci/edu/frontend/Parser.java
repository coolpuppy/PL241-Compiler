package pl241.uci.edu.frontend;

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
}

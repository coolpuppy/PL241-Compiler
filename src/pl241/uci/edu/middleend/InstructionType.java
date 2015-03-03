package pl241.uci.edu.middleend;

public enum InstructionType {
    NEG,
    ADD,
    SUB,
    MUL,
    DIV,
    CMP,
    ADDA,
    LOAD,
    STORE,
    MOVE,
    PHI,
    END,
    BRA,
    BNE,
    BEQ,
    BLE,
    BLT,
    BGE,
    BGT,
    RETURN,
    LOADADD, //For arrays only
    STOREADD, //For arrays only
    READ,
    WRITE,
    WLN
}

package pl241.uci.edu.cfg;

import pl241.uci.edu.ir.BasicBlock;
import pl241.uci.edu.ir.BlockType;
import pl241.uci.edu.ir.FunctionDecl;
import pl241.uci.edu.middleend.Instruction;

import java.util.ArrayList;
import java.util.HashMap;

/*
Date:2015/03/04
This class is used to store control flow graph.
 */
public class ControlFlowGraph {
    //this is the first block in the control flow graph
    private static BasicBlock firstBlock;

    //this stores all the blocks in the control flow graph
    private static ArrayList<BasicBlock> blocks;

    //this stores all the functions in the control flow graph
    public static HashMap<Integer,FunctionDecl> allFunctions;

    //this stores all the instructions in the control flow graph
    public static ArrayList<Instruction> allInstructions;

    public static DelUseChain delUseChain;

    public ControlFlowGraph()
    {
        blocks = new ArrayList<BasicBlock>();
        firstBlock = new BasicBlock(BlockType.NORMAL);
        allFunctions = new HashMap<Integer,FunctionDecl>();
        allInstructions = new ArrayList<Instruction>();
        delUseChain = new DelUseChain();
    }

    public static BasicBlock getFirstBlock()
    {
        return firstBlock;
    }

    public static ArrayList<BasicBlock> getBlocks()
    {
        return blocks;
    }

    public static Instruction getInstruction(int index)
    {
        return null;
    }

    public static void printInstruction()
    {

    }
}

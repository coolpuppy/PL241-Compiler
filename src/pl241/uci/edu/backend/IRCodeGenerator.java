package pl241.uci.edu.backend;

import pl241.uci.edu.cfg.ControlFlowGraph;
import pl241.uci.edu.ir.BasicBlock;
import pl241.uci.edu.middleend.Result;

/*
Date:2015/03/05
This class is used to generate the intermediate representation code, which is in the form of Instruction with pre-SSA result as operand.
 */
public class IRCodeGenerator {
    private CodeTable codeTable;

    public IRCodeGenerator()
    {
        codeTable = new CodeTable();
    }

    public void load(Result x)
    {

    }

    /**
     * This is used to fix the branch block of a instruction.
     * @param pc, the pc of a instruction.
     * @param referenceBlock, new branch block of a instruction.
     *                        As only the second operand of the instruction can indicate the branch block,
     *                        we only fix the right result.
     */
    private void fix(int pc,BasicBlock referenceBlock)
    {
        ControlFlowGraph.getInstruction(pc).getRightResult().branchBlock = referenceBlock;
    }


    private void Error(String msg)
    {
        System.out.println("IRCodeGenerator Error! " + msg);
    }
}

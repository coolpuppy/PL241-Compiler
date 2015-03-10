package pl241.uci.edu.ir;

import pl241.uci.edu.cfg.*;
import pl241.uci.edu.middleend.*;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    private BlockType type;

    private int id;

    private List<Instruction> instructions;

    //use in if and while, link to the block follow the condition
    private BasicBlock followBlock;

    //use in if, link to the else block
    private BasicBlock elseBlock;

    //use in if and while, link to the join block of
    // 1.follow blocks of if and else
    // 2.while condition block
    private BasicBlock joinBlock;

    //parent of current block
    private BasicBlock preBlock;

    //use in while loop, link back to the condition block
    private BasicBlock backBlock;

    private PhiFunctionGenerator phiFunctionGenerator;

    public BasicBlock(BlockType type)
    {
        this.type = type;
        ControlFlowGraph.getBlocks().add(this);
        this.id = ControlFlowGraph.getBlocks().size();
        phiFunctionGenerator = new PhiFunctionGenerator();
        instructions = new ArrayList<Instruction>();
        followBlock = null;
        elseBlock = null;
        joinBlock = null;
        preBlock = null;
        backBlock = null;
    }

    /**********************************get function**********************************/

    public int getId()
    {
        return this.id;
    }

    public BlockType getType()
    {
        return this.type;
    }

    public BasicBlock getFollowBlock()
    {
        return this.followBlock;
    }

    public BasicBlock getJoinBlock(){
        return this.joinBlock;
    }

    public BasicBlock getElseBlock()
    {
        return this.elseBlock;
    }

    public BasicBlock getPreBlock()
    {
        return this.preBlock;
    }

    public BasicBlock getBackBlock()
    {
        return this.backBlock;
    }

    public List<Instruction> getInstructions()
    {
        return this.instructions;
    }

    /**********************************set function**********************************/

    public void setType(BlockType type)
    {
        this.type = type;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setInstructions(List<Instruction> ins)
    {
        this.instructions = ins;
    }

    public void setFollowBlock(BasicBlock follow)
    {
        this.followBlock = follow;
    }

    public void setJoinBlock(BasicBlock join){
        this.joinBlock = join;
    }

    public void setElseBlock(BasicBlock elseBlock1)
    {
        this.elseBlock = elseBlock1;
    }

    public void setPreBlock(BasicBlock pre)
    {
        this.preBlock = pre;
    }

    public void setBackBlock(BasicBlock back)
    {
        this.backBlock = back;
    }

    /**********************************value update function**********************************/

    /**
     * This is used to update the phi function in the join block of while loop.
     * @param address, address of the value.
     * @param newssa, new SSA version of the value.
     */
    public void updateValueReferenceInWhileJoinBlock(int address,SSAValue newssa)
    {
        if(this.type != BlockType.WHILE_JOIN)
            Error("updateValueReferenceInWhileJoinBlock can only update value in WHILE_JOIN loop!");
        else
        {
            Instruction cond = findConditionInstruction(address);
            if(cond != null)
            {
                if(cond.getLeftResult().varIdent == address)
                    cond.getLeftResult().setSSAVersion(newssa.getVersion());
                else
                    cond.getRightResult().setSSAVersion(newssa.getVersion());
            }
        }
    }

    /**
     * This is used to update the value reference in a specific BasicBlock. Replace the old SSA with the new ssa.
     * @param block
     * @param address
     * @param oldssa
     * @param newssa
     */
    public void updateValueReference(BasicBlock block,int address,SSAValue oldssa,SSAValue newssa)
    {
        for(Instruction ins : block.getInstructions())
        {
            Result left = ins.getLeftResult();
            Result right = ins.getRightResult();
            if(left != null && left.type == Result.ResultType.variable && left.varIdent == address && left.ssaVersion.getVersion() == oldssa.getVersion())
                ins.getLeftResult().setSSAVersion(oldssa.getVersion());
            if(right != null && right.type == Result.ResultType.variable && right.varIdent == address && right.ssaVersion.getVersion() == oldssa.getVersion())
                ins.getRightResult().setSSAVersion(oldssa.getVersion());
        }
    }

    public void updateValueReferenceInLoopBody()
    {

    }



    /**********************************phi function**********************************/

    public void createPhiFunction(int varIdent)
    {
        this.phiFunctionGenerator.addPhiFunction(varIdent,VariableTable.getLatestVersion(varIdent));
    }

    public void updatePhiFunction(int varIdent,SSAValue ssa,BlockType type)
    {
        this.phiFunctionGenerator.updatePhiFunction(varIdent,ssa,PhiFunctionUpdateType.getBlockPhiMap().get(type));
    }

    private void Error(String msg)
    {
        System.out.println("BasicBlock Error! " + msg);
    }

    /**
     * This is used to find the condition instruction with the specific address.
     * @param address
     * @return the condition instruction.
     */
    public Instruction findConditionInstruction(int address)
    {
        for(Instruction ins : instructions)
        {
            if(ins.getOp() == InstructionType.CMP && ins.getLeftResult().varIdent == address || ins.getRightResult().varIdent == address)
                return ins;
        }
        Error("findConditionInstruction cannot find condition instruction with operand has address " + address + " ");
        return null;
    }

    /**
     * This is used to find the specific instruction with the specific pc.
     * @param pc
     */
    public Instruction findInstruction(int pc)
    {
        for(Instruction ins : instructions)
        {
            if(ins.getInstructionPC() == pc)
                return ins;
        }
        return null;
    }

    public Instruction generateInstruction(InstructionType type,Result r1,Result r2) {
        Instruction newIns = new Instruction(type, r1 == null ? null : r1.deepClone(r1), r2 == null ? null : r2.deepClone(r2));
        this.instructions.add(newIns);
        return newIns;
    }

    public PhiFunctionGenerator getPhiFunctionGenerator(){
        return phiFunctionGenerator;
    }

    //get the instruction which is the next instruction of this one
    public Instruction getNextIntruction(Instruction ins)
    {
        for(int i = 0; i< this.instructions.size()-1;i++)
        {
            if(this.instructions.get(i) == ins)
                return this.instructions.get(i+1);
        }
        return null;
    }
}

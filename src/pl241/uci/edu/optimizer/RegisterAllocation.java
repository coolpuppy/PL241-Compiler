package pl241.uci.edu.optimizer;

import pl241.uci.edu.ir.BasicBlock;
import pl241.uci.edu.middleend.Instruction;
import pl241.uci.edu.middleend.Result;
import pl241.uci.edu.middleend.InstructionType;

import java.util.HashMap;

/*
Date:2015/03/05
This is used to allocate the register for all the value in SSA form.
 */
public class RegisterAllocation {
    private final static int NUMBER_OF_REGISTER = 10;
    private boolean [] register;

    private HashMap<Integer,Integer> InstructionTOConstant = new HashMap<>();
    public static HashMap<Integer,Integer> InstructionTORegno = new HashMap<>();

    public RegisterAllocation()
    {
        register = new boolean[NUMBER_OF_REGISTER];

        for(int i = 0 ; i < NUMBER_OF_REGISTER ; i++)
            register[i] = false;
        register[0] = true;
    }

    public void optimize(BasicBlock root)
    {
        if(root != null)
        {
            for (Instruction ins : root.getInstructions())
            {
                if(ins.deleted)
                    continue;
                else if(ins.isReadInstruction())
                {
                    load(ins.getLeftResult());
                    generateArithmethicIC(ins);
                    InstructionTORegno.put(ins.getInstructionPC(),ins.getLeftResult().regno);
                }
                else if(ins.isArithOrBranch())
                    generateArithmethicIC(ins);
                else if(ins.isWriteInstruction())
                {
                    mapInstructiontoConstantorRegno(ins.getLeftResult());
                }
                else if(ins.isLoadInstruction())
                {
                    load(ins.getLeftResult());
                    InstructionTORegno.put(ins.getInstructionPC(),ins.getLeftResult().regno);
                }
            }

            optimize(root.getFollowBlock());

            optimize(root.getElseBlock());

            if(root.getJoinBlock() != null && root.getFollowBlock() == null)
                optimize(root.getJoinBlock());
        }
    }

    public int allocateRegister()
    {
        for(int i = 0 ; i< NUMBER_OF_REGISTER ;i++)
        {
            if (!register[i])
            {
                register[i] = true;
                return i;
            }
        }
        return -1;
    }

    public void deallocateRegister(int i)
    {
        register[i] = false;
    }

    private void generateArithmethicIC(Instruction ins){
        Result x = ins.getLeftResult();
        Result y = ins.getRightResult();
        if(x.type == Result.ResultType.constant && y.type == Result.ResultType.constant)
        {
            load(x);
            load(y);
            InstructionTOConstant.put(x.instrRef,x.regno);
            InstructionTOConstant.put(y.instrRef,y.regno);
        }
        else
        {
            if(x.type == Result.ResultType.variable)
                load(x);
            else if(x.type == Result.ResultType.instruction)
            {
                mapInstructiontoConstantorRegno(x);
                generateArithmethicIC(ins);
            }

            if(y.type == Result.ResultType.variable) {
                load(y);
                deallocateRegister(y.regno);
            }
            else if(y.type == Result.ResultType.instruction)
            {
                mapInstructiontoConstantorRegno(y);
                generateArithmethicIC(ins);
            }

            InstructionTORegno.put(ins.getInstructionPC(),x.regno);
        }
    }

    public void load(Result x)
    {
        if (x.type == Result.ResultType.constant) {
            x.type = Result.ResultType.register;
            if (x.value == 0)
                x.regno = 0;
            else {
                x.regno = this.allocateRegister();
            }
        } else if (x.type == Result.ResultType.variable) {
            x.type = Result.ResultType.register;
            x.regno = this.allocateRegister();
        }
    }

    public void mapInstructiontoConstantorRegno(Result x)
    {
        if(InstructionTOConstant.containsKey(x.instrRef))
        {
            x.type = Result.ResultType.instruction;
            //x.value = InstructionTOConstant.get(x.instrRef);
        }
        else if(InstructionTORegno.containsKey(x.instrRef))
        {
            x.type = Result.ResultType.instruction;
            //x.regno = InstructionTORegno.get(x.instrRef);
        }
        else
            Error("Cannot find instrunction!");
    }

    private void Error(String msg)
    {
        System.out.println("Register Allocation Error: " + msg);
    }
}

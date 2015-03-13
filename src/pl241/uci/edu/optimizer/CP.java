package pl241.uci.edu.optimizer;

import pl241.uci.edu.middleend.Result;
import pl241.uci.edu.middleend.Instruction;
import pl241.uci.edu.ir.DominatorTreeNode;

import java.util.HashMap;
import java.util.Map;

/*
Date:2015:03/08
This is used to do copy propagation.
 */
public class CP {
    public CP(){

    }

    public void CPoptimize(DominatorTreeNode root)
    {
        CPoptimizeRecursion(root,new HashMap<Result, Integer>(),new HashMap<Result, Integer>());
    }

    /**
     * Do copy propagation.
     * @param root, root node of dominator tree.
     * @param ResultTOInstruction, contain all the result can be replace by a constant
     * @param ResultTOConstant, constain all the result can be replace by a instruction
     */
    public void CPoptimizeRecursion(DominatorTreeNode root,HashMap<Result,Integer>ResultTOInstruction,HashMap<Result,Integer>ResultTOConstant)
    {
        if(root == null)
            return;

        for(Instruction ins : root.block.getInstructions())
        {
            if(ins.isReadInstruction())
            {
                int pc = ins.getInstructionPC();

                //next instruction is move instruction
                Instruction next = root.block.getNextIntruction(ins);
                Result readY = new Result();
                ins.setLeftResult(readY);
                readY.type = next.getRightResult().type;
                readY.varIdent = next.getRightResult().varIdent;
                readY.ssaVersion = next.getRightResult().ssaVersion;
                Result y = next.getRightResult();
                ResultTOInstruction.put(y, pc);

                // mark next instr as deleted
                next.deleted = true;
            }
            else if(ins.isMoveConstant())
            {
                int constant = ins.getLeftResult().value;
                ResultTOConstant.put(ins.getRightResult(),constant);
            }
            else if(ins.isMoveInstruction())
            {
                int pc = ins.getLeftResult().instrRef;
                ResultTOInstruction.put(ins.getRightResult(),pc);
            }
            //the variable is in the right result
            else if(ins.isMoveVar())
            {
                Result left = ins.getLeftResult();
                Result right = ins.getRightResult();
                if(ResultTOConstant.containsKey(left)){
                    int constant = ResultTOConstant.get(left);
                    ResultTOConstant.put(right, constant);
                    //mark instr as deleted
                    ins.deleted = true;
                }else{
                    int pc = ResultTOInstruction.get(left);
                    ResultTOInstruction.put(right, pc);
                    // mark instr as deleted
                    ins.deleted = true;
                    ins.referenceInstrId = pc;
                }
            }
            else
            {
                Result left = ins.getLeftResult();
                Result right = ins.getRightResult();

                if(left != null && left.type == Result.ResultType.variable)
                {
                    if(ResultTOConstant.containsKey(left))
                    {
                        int constant = ResultTOConstant.get(left);
                        left.type = Result.ResultType.constant;
                        left.value = constant;
                    }
                    else if(ResultTOInstruction.containsKey(left))
                    {
                        int pc = ResultTOInstruction.get(left);
                        left.type = Result.ResultType.instruction;
                        left.instrRef = pc;
                    }
                }

                if(right != null && right.type == Result.ResultType.variable)
                {
                    if(ResultTOConstant.containsKey(right))
                    {
                        int constant = ResultTOConstant.get(right);
                        right.type = Result.ResultType.constant;
                        right.value = constant;
                    }
                    else if(ResultTOInstruction.containsKey(right))
                    {
                        int pc = ResultTOInstruction.get(right);
                        right.type = Result.ResultType.instruction;
                        right.instrRef = pc;
                    }
                }
            }
        }


        //TODO:update the result in phi function
        for(Map.Entry<Integer, Instruction> entry : root.block.getPhiFunctionGenerator().getPhiInstructionMap().entrySet()){
            Instruction oldIns = entry.getValue();

        }

        for(DominatorTreeNode child : root.children){
            CPoptimizeRecursion(child, new HashMap<Result, Integer>(ResultTOInstruction),
                    new HashMap<Result, Integer>(ResultTOConstant));
        }
    }
}

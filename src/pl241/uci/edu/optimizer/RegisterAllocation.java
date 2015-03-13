package pl241.uci.edu.optimizer;

import pl241.uci.edu.cfg.ControlFlowGraph;
import pl241.uci.edu.ir.BasicBlock;
import pl241.uci.edu.ir.BlockType;
import pl241.uci.edu.ir.DominatorTreeNode;
import pl241.uci.edu.middleend.Instruction;
import pl241.uci.edu.middleend.Result;
import pl241.uci.edu.middleend.InstructionType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/*
Date:2015/03/05
This is used to allocate the register for all the value in SSA form.
 */
public class RegisterAllocation {
    private class Graph {
        private Map<Instruction, List<Instruction>> edgeList;

        public Graph() {
            edgeList = new HashMap<Instruction, List<Instruction>>();
        }

        public void addNode(Instruction instr) {
            if (!edgeList.containsKey(instr))
                edgeList.put(instr, new ArrayList<Instruction>());
        }

        public void addEdge(Instruction instr1, Instruction instr2) {
            if (!edgeList.containsKey(instr1))
                edgeList.put(instr1, new ArrayList<Instruction>());
            if (!edgeList.containsKey(instr2))
                edgeList.put(instr2, new ArrayList<Instruction>());
            if (!isEdge(instr1, instr2)) {
                edgeList.get(instr1).add(instr2);
                edgeList.get(instr2).add(instr1);
            }
        }

        public List<Instruction> getNeighbors(Instruction instr) {
            if (!edgeList.containsKey(instr))
                return new ArrayList<Instruction>();
            else
                return edgeList.get(instr);
        }

        public int getDegree(Instruction instr) {
            return edgeList.get(instr).size();
        }

        public boolean isEdge(Instruction instr1, Instruction instr2) {
            return edgeList.containsKey(instr1) && edgeList.get(instr1).contains(instr2);
        }

        public Set<Instruction> getNodes() {
            return edgeList.keySet();
        }
    }

    private class BasicBlockInfo {
        public int visited;
        public Set<Instruction> live;

        public BasicBlockInfo() {
            visited = 0;
            live = new HashSet<Instruction>();
        }
    }

    private Graph interferenceGraph;
    private Map<DominatorTreeNode, BasicBlockInfo> blockInfoMap;
    private Set<DominatorTreeNode> loopHeaders;
    private Map<Instruction, Integer> colors;

    public RegisterAllocation() {
    }

    public Map<Integer, Integer> allocate(DominatorTreeNode b) {
        // Reset allocator
        interferenceGraph = new Graph();
        blockInfoMap = new HashMap<DominatorTreeNode, BasicBlockInfo>();
        loopHeaders = new HashSet<DominatorTreeNode>();
        colors = new HashMap<Instruction, Integer>();

        calcLiveRange(b, null, 1);
        //calcLiveRange(b, null, 2);
        colorGraph();
        //saveVCGGraph("vcg/"+b.block.getId() + "interference.vcg"); // For debugging

        Map<Integer, Integer> coloredIDs = new HashMap<Integer, Integer>();
        for (Instruction i : colors.keySet()) {
            coloredIDs.put(i.getInstructionPC(), colors.get(i));
        }
        return coloredIDs;
    }

    private void colorGraph() {
        Instruction maxDegree = null;
        do {
            maxDegree = null;
            for (Instruction a : interferenceGraph.getNodes()) {
                if (!colors.containsKey(a) && (maxDegree == null || interferenceGraph.getDegree(a) > interferenceGraph.getDegree(maxDegree)))
                    maxDegree = a;
            }
            if (maxDegree == null)
                break;

            Set<Integer> taken = new HashSet<Integer>();
            for (Instruction b : interferenceGraph.getNeighbors(maxDegree)) {
                if (colors.containsKey(b))
                    taken.add(colors.get(b));
            }

            int reg = 1;
            while (taken.contains(reg))
                reg += 1;

            colors.put(maxDegree, reg);
        } while (maxDegree != null);
    }

    public void printVCG(String filename) {
        try {
            filename="vcg/"+filename + "interference.vcg";
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(filename)));

            out.write("graph: {\n");
            out.write("title: \"Interference Graph\"\n");
            out.write("layoutalgorithm: dfs\n");
            out.write("manhattan_edges: yes\n");
            out.write("smanhattan_edges: yes\n");

            Set<Instruction> done = new HashSet<Instruction>();
            for (Instruction a : interferenceGraph.getNodes()) {
                done.add(a);

                out.write("node: {\n");
                out.write("title: \"" + a.getInstructionPC() + "\"\n");
                out.write("label: \"" + "REG: " + colors.get(a) + " ::: " + a.toString() + "\"\n");
                out.write("}\n");

                for (Instruction b : interferenceGraph.getNodes()) {
                    if (interferenceGraph.isEdge(a, b) && !done.contains(b)) {
                        out.write("edge: {\n");
                        out.write("sourcename: \"" + a.getInstructionPC() + "\"\n");
                        out.write("targetname: \"" + b.getInstructionPC() + "\"\n");
                        out.write("color: blue\n");
                        out.write("}\n");
                    }
                }
            }
            out.write("}\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<Instruction> calcLiveRange(DominatorTreeNode curBlock, DominatorTreeNode preBlock, int pass) {
        Set<Instruction> live = new HashSet<Instruction>();

        if (!blockInfoMap.containsKey(curBlock))
            blockInfoMap.put(curBlock, new BasicBlockInfo());

        if (curBlock != null) {
            if (blockInfoMap.get(curBlock).visited >= pass) {
                live.addAll(blockInfoMap.get(curBlock).live);
            } else {
                blockInfoMap.get(curBlock).visited += 1;
                if (blockInfoMap.get(curBlock).visited == 2) {
                    for (DominatorTreeNode h : loopHeaders)
                        blockInfoMap.get(curBlock).live.addAll(blockInfoMap.get(h).live);
                }

                //int index = 0;
                for (DominatorTreeNode child : curBlock.getChildren()) {
                    if (curBlock.block.getType() == BlockType.WHILE_JOIN )
                        loopHeaders.add(curBlock);

                    live.addAll(calcLiveRange(child, curBlock, pass));
                    //index += 1;

                    //if (curBlock.block.getType() == BlockType.WHILE_JOIN )
                    //    loopHeaders.remove(curBlock);
                }
                List<Instruction> reverse = new ArrayList<Instruction>();
                reverse.addAll(curBlock.block.getInstructions());
                Collections.reverse(reverse);
                for (Instruction ins : reverse) {
                    if (ins.getOp() != InstructionType.PHI && (!ins.deleted) && ins.getOp()!=InstructionType.END && ins.getOp()!= InstructionType.READ && ins.getOp()!= InstructionType.WRITE) {
                        live.remove(ins);
                        interferenceGraph.addNode(ins);
                        for (Instruction other : live) {
                            interferenceGraph.addEdge(ins, other);
                        }
                        live.add(ControlFlowGraph.getInstruction(ins.getInstructionPC()));
                    }
                }

                blockInfoMap.get(curBlock).live = new HashSet<Instruction>();
                blockInfoMap.get(curBlock).live.addAll(live);
            }
            List<Instruction> reverse = new ArrayList<Instruction>();
            reverse.addAll(curBlock.block.getInstructions());
            Collections.reverse(reverse);
            for (Instruction ins : reverse) {
                if (ins.getOp() == InstructionType.PHI) {
                    live.remove(ins);
                    interferenceGraph.addNode(ins);
                    for (Instruction other : live)
                        interferenceGraph.addEdge(ins, other);
                    live.add(ControlFlowGraph.getInstruction(ins.getInstructionPC()));
                }
            }
        }
        return live;
    }
}
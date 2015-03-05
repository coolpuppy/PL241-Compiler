package pl241.uci.edu.cfg;

import pl241.uci.edu.ir.BasicBlock;
import pl241.uci.edu.ir.DominatorTreeNode;
import java.util.*;

/*
Date:2015/03/04
This class is used to generate the dominator tree.
 */
public class DominatorTreeGenerator {
    public static DominatorTreeNode root;

    private static HashSet<BasicBlock> visited;

    public DominatorTreeGenerator()
    {
        root = new DominatorTreeNode(ControlFlowGraph.getFirstBlock());
        visited = new HashSet<BasicBlock>();
        visited.add(root.block);
    }

    /**
     *  This is used to generate the dominator tree successor block of root.
     */
    public void buildDominatorTree()
    {
        if(root != null)
        {
            if(root.getBasicBlock().getFollowBlock() != null && !(visited.contains(root.getBasicBlock().getFollowBlock())))
            {
                DominatorTreeNode child = new DominatorTreeNode(root.getBasicBlock().getFollowBlock());
                root.getChildren().add(child);
                visited.add(root.getBasicBlock().getFollowBlock());
            }
            else if(root.getBasicBlock().getJoinBlock() != null && !(visited.contains(root.getBasicBlock().getJoinBlock())))
            {
                DominatorTreeNode child = new DominatorTreeNode(root.getBasicBlock().getJoinBlock());
                root.getChildren().add(child);
                visited.add(root.getBasicBlock().getJoinBlock());
            }
            else if(root.getBasicBlock().getElseBlock() != null && !(visited.contains(root.getBasicBlock().getElseBlock())))
            {
                DominatorTreeNode child = new DominatorTreeNode(root.getBasicBlock().getElseBlock());
                root.getChildren().add(child);
                visited.add(root.getBasicBlock().getElseBlock());
            }
        }
        else
            Error("Root node is null!");
    }

    private void Error(String msg)
    {
        System.out.println("DominatorTreeGenerator failed! " + msg);
    }
}

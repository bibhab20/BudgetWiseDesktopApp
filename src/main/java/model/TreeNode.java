package model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TreeNode {
    String name;
    List<TreeNode> children;
    double sortingValue;

    public TreeNode(String name, List<TreeNode> children) {
        this.name = name;
        this.children = children;
    }

    public TreeNode(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public TreeNode(String name, double sortingValue) {
        this.name = name;
        this.sortingValue = sortingValue;
        this.children = new ArrayList<>();
    }

    public void addChild(TreeNode child){
        this.children.add(child);
    }
}

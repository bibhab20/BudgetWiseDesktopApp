package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SimpleTreeNode {
    String value;
    double credit, debit, total;
    int creditCount, debitCount, totalCount;
    @Setter
    List<SimpleTreeNode> children;

    public SimpleTreeNode(String value) {
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(SimpleTreeNode child) {
        children.add(child);
    }
}


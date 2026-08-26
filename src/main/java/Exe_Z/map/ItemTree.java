/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map;

import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 *
 * @author PC
 */
@Builder
@AllArgsConstructor
public class ItemTree {

    public ItemTree(int x, int y) {
        this.xTree = x;
        this.yTree = y;
    }

    public int idTree;
    public int xTree, yTree;
}

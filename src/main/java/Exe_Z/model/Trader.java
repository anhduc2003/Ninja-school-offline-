/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.model;

import Exe_Z.item.Item;
import java.util.Vector;

/**
 *
 * @author Admin
 */
public class Trader {

    public boolean isLock;
    public Char player;
    public int coinTradeOrder;
    public Vector<Item> itemTradeOrder;
    public boolean accept;

    public Trader(Char p) {
        this.player = p;
    }

    public Char getChar() {
        return this.player;
    }
}

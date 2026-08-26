/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.model;

import Exe_Z.item.Item;
import Exe_Z.skill.Skill;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class Bijuu {

    private List<Skill> skills;
    private short[] potential;
    private Item[] equips;
    private int ppoint, spoint;

    public Bijuu() {
        this.skills = new ArrayList<>();
        this.potential = new short[5];
    }

}

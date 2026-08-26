/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.skill;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class SkillTemplate {

    public int id;
    public String name;
    public byte maxPoint;
    public byte type;
    public short iconId;
    public String description;

    public final List<Skill> skills = new ArrayList<>();

    public void addSkill(Skill skill) {
        skills.add(skill);
    }
}

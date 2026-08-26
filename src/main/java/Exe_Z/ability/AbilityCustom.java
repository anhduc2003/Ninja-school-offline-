/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.ability;

import Exe_Z.item.ItemManager;
import Exe_Z.model.Char;
import Exe_Z.option.ItemOption;
import Exe_Z.server.GameData;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Admin
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class AbilityCustom implements AbilityStrategy {

    private int hp;
    private int mp;
    private int damage, damage2;
    private int exactly;
    private int miss;
    private int fatal;
    private byte speed;
    private int resFire, resIce, resWind, reactDame;
    private ArrayList<ItemOption> options;

    @Override
    public void setAbility(Char owner) {
        owner.options = new int[ItemManager.getInstance().getOptionSize()];
        if (options != null) {
            for (ItemOption io : options) {
                owner.options[io.optionTemplate.id] = io.param;
            }
        }
        int length = GameData.getInstance().getOptionTemplates().size();
        owner.optionsSupportSkill = new int[length];
        owner.maxHP = this.hp;
        owner.maxMP = this.mp;
        owner.damage = this.damage;
        owner.damage2 = this.damage2;
        owner.exactly = this.exactly;
        owner.miss = this.miss;
        owner.fatal = this.fatal;
        owner.speed = this.speed;
        owner.resFire = this.resFire;
        owner.resIce = this.resIce;
        owner.resWind = this.resWind;
        owner.reactDame = this.reactDame;
    }

    public void addOption(int id, int param) {
        this.options.add(new ItemOption(id, param));
    }

}

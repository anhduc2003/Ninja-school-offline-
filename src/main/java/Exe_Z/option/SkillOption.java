/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.option;

import Exe_Z.server.GameData;
import Exe_Z.skill.SkillOptionTemplate;

public class SkillOption {

    public SkillOption(int templateId, int param) {
        this.param = param;
        this.optionTemplate = GameData.getInstance().getOptionTemplates().get(templateId);
    }

    public int param;
    public SkillOptionTemplate optionTemplate;
}

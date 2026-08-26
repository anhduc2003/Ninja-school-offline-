/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.model;

import Exe_Z.skill.SkillTemplate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
@AllArgsConstructor
@Builder
@Getter
public class Clazz {

    private int id;
    private String name;
    @Builder.Default
    private List<SkillTemplate> skillTemplates = new ArrayList<>();

    public void addSkillTemplate(SkillTemplate skillTemplate) {
        this.skillTemplates.add(skillTemplate);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
@Builder
@AllArgsConstructor
@Getter
public class MobInfo {

    private int mapID;
    private int mobID;
    private int level;

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.mob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class MobPosition {

    private short id;
    private short x;
    private short y;
    private boolean isBeast;

}

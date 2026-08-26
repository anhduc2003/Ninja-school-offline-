/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author PC
 */
@NoArgsConstructor
@Setter
@Getter
@Builder
@AllArgsConstructor
public class SmallImage {

    public short id;
    public short x;
    public short y;
    public short w;
    public short h;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.event.eventpoint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
@Setter
@Getter
@AllArgsConstructor

public class Point {

    private String key;
    private int point;
    private int rewarded;

    public void addPoint(int point) {
        this.point += point;
    }

    public void subPoint(int point) {
        this.point -= point;
    }
}

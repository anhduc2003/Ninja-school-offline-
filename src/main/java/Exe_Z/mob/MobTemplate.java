/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.mob;

import Exe_Z.model.Frame;
import Exe_Z.model.ImageInfo;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author pika
 */
@Getter
@Setter
public class MobTemplate {

    public int id;
    public String name;
    public byte type;
    public int hp;
    public short level;
    public byte rangeMove;
    public byte speed;
    public ImageInfo[] imgInfo;
    public Frame[] frameBoss;
    public byte[] frameBossMove;
    public byte[][] frameBossAttack;
    public byte numberImage;
    public byte typeFly;
    public byte[][] frameChar = new byte[4][];
    public short[] sequence;
    public byte[] indexSplash = new byte[4];
    private boolean isBoss;
}

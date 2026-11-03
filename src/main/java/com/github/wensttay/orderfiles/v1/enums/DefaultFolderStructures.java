/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template directory, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.wensttay.orderfiles.v1.enums;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 *
 * @author Wensttay
 */
public enum DefaultFolderStructures {
    SIMPLE(new SimpleDateFormat("yyyy.MM.dd")),
    TREE(new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd")),
    TREE_MOTH(new SimpleDateFormat("yyyy" + File.separator + "MMMM" + File.separator + "dd"));
    
    private final SimpleDateFormat simpleDateFormat;

    private DefaultFolderStructures(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }
}

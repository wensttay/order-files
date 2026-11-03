package com.github.wensttay.orderfiles.v1;

import com.github.wensttay.orderfiles.v1.enums.DefaultFolderStructures;
import java.io.File;

/**
 * @version
 * @author Wensttay de Sousa Alencar <yattsnew@gmail.com>
 * @date 16/01/2017 - 12:00:00
 */
public class Start {
    
    public static void main(String[] args) {

        String path = "/home/wensttay/Imagens/Backups/iphone-5s-backup-08-06-2017";
        OrderFiles orderFiles;
        orderFiles = new OrderFiles(DefaultFolderStructures.SIMPLE);

        orderFiles.orderByDate(new File(path));
    }
}

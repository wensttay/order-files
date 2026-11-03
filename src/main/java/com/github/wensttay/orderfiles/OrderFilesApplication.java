package com.github.wensttay.orderfiles;

import com.github.wensttay.orderfiles.v2.OrderFilesService;
import com.github.wensttay.orderfiles.v2.SortParams;

public class OrderFilesApplication {

    private static OrderFilesService orderFilesService;

    public static void main(String[] args) {
//        if (args.length < 2) {
//            throw new IllegalArgumentException("Usage: OrderFilesApplication <originPath> <targetPath>");
//        }
//        final String originPath = args[0];
//        final String targetPath = args[1];
        final String originPath = "/Users/wensttay/Pictures";
        final String targetPath = "/Volumes/SSD Externo/Test";

        orderFilesService = new OrderFilesService(new SortParams(originPath, targetPath));
        orderFilesService.orderMedia();
    }

}

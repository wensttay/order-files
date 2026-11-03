package com.github.wensttay.orderfiles.v2;

import java.io.File;

public class SortParams  {

    private File originDirectory;
    private File targetDirectory;

    public SortParams(
            final String originPath,
            final String targetPath
    ) {
        this.originDirectory = new File(originPath);
        this.targetDirectory = new File(targetPath);
    }

    public File getOriginFile() {
        return originDirectory;
    }

    public File getTargetFile() {
        return targetDirectory;
    }
}

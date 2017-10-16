package com.bydauto.tsbutter;

/**
 * Created by byd_tw on 2017/9/13.
 */

public class Model {
    private String name;
    private int size;
    private String time;

    public Model(String descriptor) {
//        {"2017-09-13-16-31-27.MP4":"188743680 bytes|2017-09-13 16:33:26"}
        descriptor = descriptor.replaceAll("[{\"}]", "");
        int index = descriptor.indexOf(":");
        name = descriptor.substring(0, index);

        descriptor = descriptor.substring(index + 1);
        index = descriptor.indexOf(" ");
        if (descriptor.contains("|")) {
            size = Integer.parseInt(descriptor.substring(0, index));

            index = descriptor.indexOf("|");
            time = descriptor.substring(index + 1);
        } else if (descriptor.contains("bytes")) {
            size = Integer.parseInt(descriptor.substring(0, index));
            time = null;
        } else {
            size = -1;
            time = descriptor.substring(index + 1);
        }
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public String getTime() {
        return time;
    }

}

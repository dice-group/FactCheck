package org.aksw.defacto.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileReader {
    //className like "org.aksw.defacto.Defacto"
    //fileName like "defacto.ini"
    public static File read(String className,String fileName) throws ClassNotFoundException, IOException {
        Class cls = Class.forName(className);

        // returns the ClassLoader object associated with this Class
        ClassLoader cLoader = cls.getClassLoader();

        // input stream
        InputStream inputStream = cLoader.getResourceAsStream(fileName);

        File targetFile = Stream2file.Convert(inputStream);

        return targetFile;
    }
}

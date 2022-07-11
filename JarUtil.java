package com.joojn.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtil {

    public static String getJarEntryToString(JarFile file, String name) throws IOException {
        JarEntry entry = getJarEntry(file, name);
        if(entry == null) return null;

        InputStream stream = file.getInputStream(entry);

        return IOUtils.toString(stream, StandardCharsets.UTF_8);
    }

    public static JarEntry getJarEntry(JarFile file, String name) {

        for (Enumeration<JarEntry> list = file.entries(); list.hasMoreElements(); )
        {
            JarEntry entry = list.nextElement();

            if (entry.getName().contains(name)) {
                return entry;
            }
        }

        return null;
    }

    public static <T, V> List<Pair<T, V>> loadPatches(JarFile file, String name, PatchEntry entryCall) throws IOException {
        String contents = getJarEntryToString(file, name);
        if(contents == null) return null;

        List<Pair<T, V>> map = new ArrayList<>();

        String[] lines = contents.split("\n");

        for(String line : lines)
        {
            String patchName = line.split(" ")[0];
            String value = line.split(" ")[1];

            map.add((Pair<T, V>) entryCall.entry(patchName, value));
        }

        return map;
    }

    public interface PatchEntry{
        Pair<Object, Object> entry(String name, String value);
    }

}

package xyz.fxti.zipstrings;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class Main {
    public static void main(String[] args) throws IOException {
        run(args[0]);

        System.exit(0);
    }

    static void run(String zipPath) throws IOException {
        final Map<String, BigInteger> res = new HashMap<>();

        final ZipFile archive = new ZipFile(zipPath);
        if (zipPath.endsWith(".aar")) {
            for (Enumeration<? extends ZipEntry> entries = archive.entries(); entries.hasMoreElements(); ) {
                final ZipEntry entry = entries.nextElement();
                final String entryName = entry.getName();

                if (entryName.equals("classes.jar")
                        || (entryName.startsWith("libs/") && entryName.endsWith(".jar"))) {
                    final InputStream inputStream = archive.getInputStream(entry);
                    final ZipInputStream zipInputStream = new ZipInputStream(inputStream);

                    ZipEntry zipEntry = null;
                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        if (zipEntry.getName().endsWith(".class")) {
                            merge(res, runInner(zipInputStream.readAllBytes()));
                        }
                    }

                    zipInputStream.close();
                    inputStream.close();
                }
            }
        } else {
            for (Enumeration<? extends ZipEntry> entries = archive.entries(); entries.hasMoreElements(); ) {
                final ZipEntry entry = entries.nextElement();
                final String entryName = entry.getName();

                if (entryName.endsWith(".class")) {
                    final InputStream inputStream = archive.getInputStream(entry);
                    merge(res, runInner(inputStream.readAllBytes()));
                    inputStream.close();
                }
            }
        }
        archive.close();

        println(res);
    }

    static void merge(Map<String, BigInteger> res, Map<String, BigInteger> tmp) {
        for (Entry entry : tmp.entrySet()) {
            final String key = (String) entry.getKey();
            res.put(key, ((BigInteger) entry.getValue()).add(res.containsKey(key) ? res.get(key) : BigInteger.ZERO));
        }
    }

    static Map<String, BigInteger> runInner(byte[] bytes) {
        ModClassReader reader = new ModClassReader(bytes);
        reader.accept(new ClassNode(), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return reader.getRecord();
    }

    static void println(Map<String, BigInteger> content) {
        System.out.println(new Gson().toJson(content.entrySet().stream().map(x -> new Record(x.getKey(), x.getValue()))
                .collect(Collectors.toList())));
    }
}

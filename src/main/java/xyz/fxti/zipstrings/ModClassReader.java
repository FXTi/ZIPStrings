package xyz.fxti.zipstrings;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassReader;

public class ModClassReader extends ClassReader {

    public Map<String, BigInteger> getRecord() {
        return record;
    }

    final private Map<String, BigInteger> record = new HashMap<>();

    public ModClassReader(byte[] classFile) {
        super(classFile);
    }

    @Override
    public Object readConst(int constantPoolEntryIndex, char[] charBuffer) {
        final Object res = super.readConst(constantPoolEntryIndex, charBuffer);

        Field classFileBufferFiled = null;
        Field cpInfoOffsetsFiled = null;
        try {
            classFileBufferFiled = this.getClass().getSuperclass().getDeclaredField("classFileBuffer");
            cpInfoOffsetsFiled = this.getClass().getSuperclass().getDeclaredField("cpInfoOffsets");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        classFileBufferFiled.setAccessible(true);
        cpInfoOffsetsFiled.setAccessible(true);
        byte[] classFileBuffer = new byte[0];
        int[] cpInfoOffsets = new int[0];
        try {
            classFileBuffer = (byte[]) classFileBufferFiled.get(this);
            cpInfoOffsets = (int[]) cpInfoOffsetsFiled.get(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Field constantStringTagField = null;
        try {
            constantStringTagField = Class.forName("org.objectweb.asm.Symbol")
                    .getDeclaredField("CONSTANT_STRING_TAG");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        constantStringTagField.setAccessible(true);
        int CONSTANT_STRING_TAG = 0;
        try {
            CONSTANT_STRING_TAG = (int) constantStringTagField.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (res != null && CONSTANT_STRING_TAG == classFileBuffer[cpInfoOffsets[constantPoolEntryIndex] - 1]) {
            final String s = (String) res;

            this.record.put(s, this.record.containsKey(s) ? this.record.get(s).add(BigInteger.ONE) : BigInteger.ONE);
        }

        return res;
    }
}

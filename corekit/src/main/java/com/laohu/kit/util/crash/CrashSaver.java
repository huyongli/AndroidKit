package com.laohu.kit.util.crash;

import android.content.Context;
import android.text.TextUtils;

import com.laohu.kit.util.MD5;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

class CrashSaver {

    public static void save(Context context, Throwable ex, boolean uncaught, Map<String, String> appendSnapshot) {
        Writer writer = null;
        PrintWriter printWriter = null;
        String stackTrace = "";
        try {
            writer = new StringWriter();
            printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            stackTrace = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (printWriter != null) {
                printWriter.close();
            }
        }
        String signature = stackTrace.replaceAll("\\([^\\(]*\\)", "");
        String filename = MD5.getStringMD5(signature);
        if (TextUtils.isEmpty(filename)) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String timestamp = sdf.format(date);
        BufferedWriter mBufferedWriter = null;
        try {
            File mFile = new File(context.getExternalFilesDir(null), "crash/" + filename + ".log");
            File pFile = mFile.getParentFile();
            if (!pFile.exists()) {// 如果文件夹不存在，则先创建文件夹
                pFile.mkdirs();
            }
            int count = 1;
            if (mFile.exists()) {
                LineNumberReader reader = null;
                try {
                    reader = new LineNumberReader(new FileReader(mFile));
                    String line = reader.readLine();
                    if (line != null && line.startsWith("count")) {
                        int index = line.indexOf(":");
                        if (index != -1) {
                            String count_str = line.substring(++index);
                            if (count_str != null) {
                                count_str = count_str.trim();
                                count = Integer.parseInt(count_str);
                                count++;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
                mFile.delete();
            }

            mFile.createNewFile();

            mBufferedWriter = new BufferedWriter(new FileWriter(mFile, true));// 追加模式写文件
            Map<String, String> snapshot = CrashSnapshot.snapshot(context, uncaught, timestamp, count);
            if (appendSnapshot != null) {
                snapshot.putAll(appendSnapshot);
            }
            String content = buildSnapshotString(snapshot, stackTrace);
            mBufferedWriter.append(content);
            mBufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mBufferedWriter != null) {
                try {
                    mBufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String buildSnapshotString(Map<String, String> snapshot, String trace) {
        Iterator<Map.Entry<String, String>> iterator = snapshot.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry != null) {
                sb.append(entry.getKey()).append(entry.getValue());
                sb.append(System.getProperty("line.separator"));
            }
        }
        sb.append(System.getProperty("line.separator"));
        sb.append("**----------------------***");
        sb.append(System.getProperty("line.separator"));
        sb.append(trace);
        sb.append(System.getProperty("line.separator"));
        sb.append(System.getProperty("line.separator"));
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }
}

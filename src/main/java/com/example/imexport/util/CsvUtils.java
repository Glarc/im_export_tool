package com.example.imexport.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 工具类
 * 提供 CSV 文件的读取和写入功能
 */
public class CsvUtils {

    /**
     * 从输入流读取 CSV 数据
     *
     * @param inputStream 输入流
     * @param modelClass 数据模型类
     * @param headers 预期的列名数组
     * @param <T> 数据模型类型
     * @return 解析后的数据列表
     * @throws IOException 读取异常
     */
    public static <T> List<T> readCsv(InputStream inputStream, Class<T> modelClass, String[] headers) throws IOException {
        List<T> result = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String[]> allRows = reader.readAll();
            
            if (allRows.isEmpty()) {
                return result;
            }
            
            // 跳过表头（第一行）
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                T instance = parseRow(row, modelClass, headers);
                if (instance != null) {
                    result.add(instance);
                }
            }
            
        } catch (CsvException e) {
            throw new IOException("CSV 解析失败", e);
        }
        
        return result;
    }

    /**
     * 将数据写入 CSV 输出流
     *
     * @param outputStream 输出流
     * @param data 数据列表
     * @param headers 列名数组
     * @param <T> 数据模型类型
     * @throws IOException 写入异常
     */
    public static <T> void writeCsv(OutputStream outputStream, List<T> data, String[] headers) throws IOException {
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            
            // 写入表头
            writer.writeNext(headers);
            
            // 写入数据
            if (data != null && !data.isEmpty()) {
                for (T item : data) {
                    String[] row = toRow(item, headers.length);
                    writer.writeNext(row);
                }
            }
        }
    }

    /**
     * 解析单行数据为对象
     */
    private static <T> T parseRow(String[] row, Class<T> modelClass, String[] headers) {
        try {
            T instance = modelClass.newInstance();
            Field[] fields = modelClass.getDeclaredFields();
            
            for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                if (i < fields.length) {
                    Field field = fields[i];
                    field.setAccessible(true);
                    
                    String value = row[i];
                    if (value != null && !value.trim().isEmpty()) {
                        setFieldValue(field, instance, value);
                    }
                }
            }
            
            return instance;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将对象转换为行数组
     */
    private static <T> String[] toRow(T item, int columnCount) {
        String[] row = new String[columnCount];
        
        try {
            Field[] fields = item.getClass().getDeclaredFields();
            
            for (int i = 0; i < Math.min(fields.length, columnCount); i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Object value = field.get(item);
                row[i] = value != null ? value.toString() : "";
            }
        } catch (Exception e) {
            // 忽略异常，返回部分填充的数组
        }
        
        return row;
    }

    /**
     * 设置字段值（支持基本类型和字符串）
     */
    private static void setFieldValue(Field field, Object instance, String value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        
        if (fieldType == String.class) {
            field.set(instance, value);
        } else if (fieldType == int.class || fieldType == Integer.class) {
            field.set(instance, Integer.parseInt(value));
        } else if (fieldType == long.class || fieldType == Long.class) {
            field.set(instance, Long.parseLong(value));
        } else if (fieldType == double.class || fieldType == Double.class) {
            field.set(instance, Double.parseDouble(value));
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            field.set(instance, Boolean.parseBoolean(value));
        } else {
            // 其他类型尝试设置字符串值
            field.set(instance, value);
        }
    }
}

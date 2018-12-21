package file;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author yangzhenkun
 * @create 2018-12-13 17:33
 */
public class ExportUtil {


    public static <T> void export(List<T> data, String filePath) {

        int rowIndex = 0;

        Class clazz = data.get(0).getClass();

        Field[] allFields = clazz.getDeclaredFields();

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();

        HSSFRow hssfRow = hssfSheet.createRow(rowIndex++);

        int colmnIndex = 0;

        for (Field field : allFields) {
            field.setAccessible(true);
            HSSFCell hssfCell = hssfRow.createCell(colmnIndex++);
            hssfCell.setCellValue(field.getName());
        }

        for (T datum : data) {

            colmnIndex = 0;
            HSSFRow row = hssfSheet.createRow(rowIndex++);

            for (Field field : allFields) {
                field.setAccessible(true);
                HSSFCell hssfCell = row.createCell(colmnIndex++);
                try {
                    Object temp = field.get(datum);
                    if (temp != null) {
                        hssfCell.setCellValue(temp.toString());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }


        try {
            hssfWorkbook.write(new File(filePath));
            hssfWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void wordTemplateReplace(Map<String, String> data) {

        try {
            FileInputStream fis = new FileInputStream(new File("src/main/resources/template.doc"));
            HWPFDocument hdt = new HWPFDocument(fis);
            Range range = hdt.getRange();

            data.forEach((k, v) -> {
                String nk = "{" + k + "}";
                range.replaceText(nk, v);
            });

            hdt.write(new File("src/main/resources/new.doc"));
            hdt.close();
            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
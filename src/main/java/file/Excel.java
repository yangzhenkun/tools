package file;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 可以分批导入excel
 * @see ExportUtil#export(List, String) 只能导入一次
 *
 * @see Excel#add(List) 方法可以多次添加，最后调用save即可保存
 *
 * 导出excel
 */
public class Excel {

    private String path = null;
    private int rowIndex = 0;
    private HSSFWorkbook hssfWorkbook;
    private HSSFSheet sheet;
    private Field[] fields;

    public <T> Excel(String path, Class<T> clazz) {
        this.path = path;
        rowIndex = 0;
        hssfWorkbook = new HSSFWorkbook();
        sheet = hssfWorkbook.createSheet();

        fields = clazz.getDeclaredFields();

        genericTitle();
    }

    private void genericTitle() {

        HSSFRow row = sheet.createRow(rowIndex);

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(fields[i].getName());

        }

        rowIndex++;

    }

    public <T> void add(List<T> data) {

        if (CollectionUtils.isNotEmpty(data)) {

            data.forEach(d -> {
                HSSFRow row = sheet.createRow(rowIndex);

                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    HSSFCell cell = row.createCell(i);
                    try {
                        Object o = fields[i].get(d);
                        if (o != null) {
                            cell.setCellValue(o.toString());
                        }

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                rowIndex++;
            });


        }


    }

    public void save() {

        try {
            hssfWorkbook.write(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                hssfWorkbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("保存成功");
        }

    }

}

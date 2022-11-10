package com.bjpowernode.crm.test;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.util.List;

//Spring整合Junit单元测试
@RunWith(SpringJUnit4ClassRunner.class)
//运行测试方法时,加载Spring容器
@ContextConfiguration("classpath:spring/applicationContext-service.xml")
public class PoiTest {

    @Test
    public void testReadExcel() throws IOException {
        //读取Excel文件中的内容,输出到控制台即可
        //将Excel文件读取成输入流对象
        InputStream in = new FileInputStream("/Users/limingxuan/Desktop/Activity.xls");

        //基于输入流对象,加载工作簿对象
        /*
            Workbook工作簿对象
                HSSFWorkbook    -> xxx.xls  低版本
                XSSFWorkbook    -> xxx.xlsx 高版本
         */
        Workbook workbook = new HSSFWorkbook(in);

        //获取Excel文件中的行对象
        //获取第一页的数据
        Sheet sheet = workbook.getSheetAt(0);

        //获取最后的行号
        int lastRowNum = sheet.getLastRowNum();

        for(int i=0; i<=lastRowNum; i++){
            //获取行对象
            Row row = sheet.getRow(i);

            //获取单元格中的数据,输出到控制台
            //每行只有5个单元格,直接获取
            String name = row.getCell(0).getStringCellValue();
            String startDate = row.getCell(1).getStringCellValue();
            String endDate = row.getCell(2).getStringCellValue();
            String cost = row.getCell(3).getStringCellValue();
            String description = row.getCell(4).getStringCellValue();

            System.out.println(name);
            System.out.println(startDate);
            System.out.println(endDate);
            System.out.println(cost);
            System.out.println(description);
            System.out.println("----------");
        }

        workbook.close();
        in.close();
    }


    @Test
    public void testWriteExcel() throws IOException {
        //将数据写入到Excel文件中
        //创建工作簿对象
        Workbook workbook = new HSSFWorkbook();

        //根据工作簿对象,创建页码对象
        Sheet sheet = workbook.createSheet();

        //根据页码对象,创建行对象
        //第一行是表头数据
        Row r = sheet.createRow(0);

        r.createCell(0).setCellValue("id");
        r.createCell(1).setCellValue("name");
        r.createCell(2).setCellValue("age");

        //for循环,生成数据
        for(int i=0; i<10; i++){
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(i+1);
            row.createCell(1).setCellValue("张"+i);
            row.createCell(2).setCellValue("1"+i);
        }

        //将工作簿对象,输出到指定目录中
        workbook.write(
                //这里一定要指定具体的路径及文件名称
                new FileOutputStream("/Users/limingxuan/Desktop/Activity-Test.xls")
        );
    }

}

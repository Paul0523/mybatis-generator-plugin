package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.tools.DBHelper;
import com.itfsw.mybatis.generator.plugins.tools.MyBatisGeneratorTool;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.MyBatisGenerator;

/**
 * @author fangzhipeng
 * @date 2018/11/23
 */
public class TableBatchInsertPluginTest {
    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("scripts/TableBatchInsertPlugin/init.sql");
    }


    @Test
    public void testTableSplit() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/TableBatchInsertPlugin/mybatis-generator-TableBatchInsertPlugin.xml");
        MyBatisGenerator myBatisGenerator = tool.generate();
        myBatisGenerator.getGeneratedXmlFiles();
    }

}

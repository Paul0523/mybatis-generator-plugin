/*
 * Copyright (c) 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import com.itfsw.mybatis.generator.plugins.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * 批量插入插件
 * ---------------------------------------------------------------------------
 *
 * @author: hewei
 * @time:2017/1/13 9:33
 * ---------------------------------------------------------------------------
 */
public class TableBatchInsertPlugin extends BasePlugin {
    public static final String METHOD_BATCH_INSERT = "batchInsertSelective";  // 方法名

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(List<String> warnings) {

        // 插件使用前提是数据库为MySQL或者SQLserver，因为返回主键使用了JDBC的getGenereatedKeys方法获取主键
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.microsoft.jdbc.sqlserver.SQLServer".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.microsoft.sqlserver.jdbc.SQLServerDriver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.mysql.cj.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件使用前提是数据库为MySQL或者SQLserver，因为返回主键使用了JDBC的getGenereatedKeys方法获取主键！");
            return false;
        }

        return super.validate(warnings);
    }


    /**
     * Java Client Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {


        // 1. batchInsert
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();

        listType.addTypeArgument(introspectedTable.getRules().calculateAllFieldsClass());
        Method mBatchInsert;
        if (split(introspectedTable)) {
            interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
            mBatchInsert = JavaElementGeneratorTools.generateMethod(
                    METHOD_BATCH_INSERT,
                    JavaVisibility.DEFAULT,
                    FullyQualifiedJavaType.getIntInstance(),
                    new Parameter(listType, "list", "@Param(\"list\")"),
                    new Parameter(FullyQualifiedJavaType.getStringInstance(), "index", "@Param(\"index\")")

            );
        } else {
            mBatchInsert = JavaElementGeneratorTools.generateMethod(
                    METHOD_BATCH_INSERT,
                    JavaVisibility.DEFAULT,
                    FullyQualifiedJavaType.getIntInstance(),
                    new Parameter(listType, "list")

            );
        }

        commentGenerator.addGeneralMethodComment(mBatchInsert, introspectedTable);
        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, mBatchInsert);
        logger.debug("itfsw(批量插入插件):" + interfaze.getType().getShortName() + "增加batchInsertSelective方法。");

        return true;
    }

    /**
     * SQL Map Methods 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        // 1. batchInsert
        XmlElement batchInsertEle = new XmlElement("insert");
        batchInsertEle.addAttribute(new Attribute("id", METHOD_BATCH_INSERT));

        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(batchInsertEle);

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(batchInsertEle, introspectedTable);

        String tableName = split(introspectedTable) ?
                introspectedTable.getFullyQualifiedTableNameAtRuntime() + "_#{index}" : introspectedTable.getFullyQualifiedTableNameAtRuntime();
        batchInsertEle.addElement(new TextElement("insert into " + tableName));
        StringBuilder sb = new StringBuilder("(");
        for (IntrospectedColumn column : ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {
            sb.append(column.getActualColumnName()).append(",");
        }
        batchInsertEle.addElement(new TextElement(sb.substring(0, sb.length() - 1) + ")"));

        // 添加foreach节点
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));

        sb = new StringBuilder("(\n");
        for (IntrospectedColumn column : ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {
            sb.append(getColumnStr(column)).append(",\n");
        }
        foreachElement.addElement(new TextElement(sb.substring(0, sb.length() - 2) + "\n      )"));
        // values 构建
        batchInsertEle.addElement(new TextElement("values"));
        batchInsertEle.addElement(foreachElement);
        document.getRootElement().addElement(batchInsertEle);
        logger.debug("itfsw(批量插入插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加batchInsert实现方法。");


        return true;
    }

    private String getColumnStr(IntrospectedColumn column) {
        String commonTemplate =
                "        #{item.javaProperty}";
        String template =
                "        <if test=\"item.javaProperty != null\">\n" +
                "          #{item.javaProperty}\n" +
                "        </if>\n" +
                "        <if test=\"item.javaProperty == null\">\n" +
                "          defaultValue\n" +
                "        </if>";
        if (column.getDefaultValue() == null) {
            return commonTemplate.replaceAll("javaProperty", column.getJavaProperty()).replaceAll("columnName", column.getActualColumnName());
        } else {
            return template.replaceAll("javaProperty", column.getJavaProperty()).replaceAll("columnName", column.getActualColumnName()).replaceAll("defaultValue", column.getDefaultValue());
        }
    }

    /**
     * 判断是否要进行分表
     *
     * @param introspectedTable 表信息
     * @return
     */
    private boolean split(IntrospectedTable introspectedTable) {
        String split = introspectedTable.getTableConfigurationProperty("split");
        if ("true".equals(split)) {
            return true;
        }
        return false;
    }

}
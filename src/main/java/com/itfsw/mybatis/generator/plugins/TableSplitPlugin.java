package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.BeanUtils;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * @author fangzhipeng
 * @date 2018/11/23
 */
public class TableSplitPlugin extends BasePlugin {


    // record 段修改

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!split(introspectedTable)) {
            return true;
        }
        Field index = JavaElementGeneratorTools.generateField("index", JavaVisibility.PRIVATE, FullyQualifiedJavaType.getStringInstance(), null);
        commentGenerator.addFieldComment(index, introspectedTable);
        topLevelClass.addField(index);

        Method setter = JavaElementGeneratorTools.generateSetterMethod(index);
        commentGenerator.addGeneralMethodComment(setter, introspectedTable);
        topLevelClass.addMethod(setter);

        Method getter = JavaElementGeneratorTools.generateGetterMethod(index);
        commentGenerator.addGeneralMethodComment(getter, introspectedTable);
        topLevelClass.addMethod(getter);

        return true;
    }

    // example 段修改

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!split(introspectedTable)) {
            return true;
        }
        Field index = JavaElementGeneratorTools.generateField("index", JavaVisibility.PRIVATE, FullyQualifiedJavaType.getStringInstance(), null);
        commentGenerator.addFieldComment(index, introspectedTable);
        topLevelClass.addField(index);

        Method setter = JavaElementGeneratorTools.generateSetterMethod(index);
        commentGenerator.addGeneralMethodComment(setter, introspectedTable);
        topLevelClass.addMethod(setter);

        Method getter = JavaElementGeneratorTools.generateGetterMethod(index);
        commentGenerator.addGeneralMethodComment(getter, introspectedTable);
        topLevelClass.addMethod(getter);

        // 添加orderBy
        Method orderByMethod = JavaElementGeneratorTools.generateMethod(
                "index",
                JavaVisibility.PUBLIC,
                topLevelClass.getType(),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "index")
        );
        commentGenerator.addGeneralMethodComment(orderByMethod, introspectedTable);
        orderByMethod = JavaElementGeneratorTools.generateMethodBody(
                orderByMethod,
                "this.setIndex(index);",
                "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, orderByMethod);
        return true;
    }

    // mapper 修改段

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (!split(introspectedTable)) {
            return true;
        }
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        method.getParameters().get(0).getAnnotations().add("@Param(\"id\")");
        Parameter index = new Parameter(FullyQualifiedJavaType.getStringInstance(), "index", "@Param(\"index\")");
        method.addParameter(index);
        return true;
    }

    // xml 修改段


    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return joinIndex(element, introspectedTable);
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return joinIndex(element, introspectedTable);
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return joinIndex(element, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return joinIndex(element, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return joinIndex(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return joinIndex(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return joinIndex(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return joinIndex(element, introspectedTable);
    }

    /**
     * 向xml table名中加入index 后缀
     * @param element
     * @param introspectedTable
     * @return
     */
    private boolean joinIndex(XmlElement element, IntrospectedTable introspectedTable) {
        if (!split(introspectedTable)) {
            return true;
        }
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        for (Element elementElement : element.getElements()) {
            if (!(elementElement instanceof TextElement)) {
                continue;
            }
            TextElement textElement = (TextElement) elementElement;
            if (textElement.getContent().indexOf(tableName) == -1) {
                continue;
            }
            try {
                BeanUtils.setProperty(textElement, "content", textElement.getContent().replace(tableName, tableName + "_${index}"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 判断是否要进行分表
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

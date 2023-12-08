<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Hello World!</title>
</head>
<body>

<#-- list 数据的展示 -->
<b>展示list中的stu数据:</b>
<br>
<br>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#if stus??>
        <#list stus as stu>
            <#if stu.name='小红'>
                <tr style="color: red">
                    <td>${stu_index+1}</td>
                    <td>${stu.name}</td>
                    <td>${stu.age}</td>
                    <td>${stu.money}</td>
                </tr>
            <#else>
                <tr>
                    <td>${stu_index+1}</td>
                    <td>${stu.name}</td>
                    <td>${stu.age}</td>
                    <td>${stu.money}</td>
                </tr>
            </#if>
        </#list>
    </#if>

    内建函数：
    stus集合的大小：${stus?size}
    <br>
    显示年月日: ${today?date}  <!-- 2023-11-22 -->
    <br>
    显示时分秒: ${today?time}  <!-- 17:04:55 -->
    <br>
    显示日期+时间：${today?datetime}  <!-- 2023-11-22 17:04:55 -->
    <br>
    自定义格式化:${today?string("yyyy年MM月dd日HH时mm分ss秒")}  <!-- 2023年11月 -->
    <br>

    ${point?c}

    <br>

    <#assign json="{'bank':'工商银行','account':'10101920201920212'}" />
    <#assign obj=json?eval />
    开户行：${obj.bank}
    <br>
    账号：${obj.account}


</table>
<hr>

<#-- Map 数据的展示 -->
<b>map数据的展示：</b>
<br/><br/>
<a href="###">方式一：通过map['keyname'].property</a><br/>
输出stu1的学生信息：<br/>
姓名：${stuMap['stu1'].name}<br/>
年龄：${stuMap['stu1'].age}<br/>
<br/>
<a href="###">方式二：通过map.keyname.property</a><br/>
输出stu2的学生信息：<br/>
姓名：${stuMap.stu2.name}<br/>
年龄：${stuMap.stu2.age}<br/>

<br/>
<a href="###">遍历map中两个学生信息：</a><br/>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#list stuMap?keys as key >
        <tr>
            <td>${key_index}</td>
            <td>${stuMap[key].name}</td>
            <td>${stuMap[key].age}</td>
            <td>${stuMap[key].money}</td>
        </tr>
    </#list>
</table>
<hr>

</body>
</html>
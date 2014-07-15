package inetintelliprocess.util;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.processor.barschart.BarChartInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.MDC;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;


public class GenerateWord {
    // 代表一个word 程序
    private ActiveXComponent MsWordApp = null;
    // 代表进行处理的word 文档
    private Dispatch document = null;

    private Dispatch selection = null;

    public static String jpegPosition = Config.read("PICTURE_PATH");
    public static String docPosition = Config.read("DOCUMENT_PATH");


    public GenerateWord(){
        if (MsWordApp == null) {
            MsWordApp = new ActiveXComponent("Word.Application");
        }

    }
    public Dispatch getSelection()
    {
        return this.selection;
    }
    // 设置是否在前台打开 word 程序 ，
    public void setVisible(boolean visible) {
        MsWordApp.setProperty("Visible", new Variant(visible));

        // 这一句作用相同
        // Dispatch.put(MsWordApp, "Visible", new Variant(visible));
    }

    // 创建一个新文档
    public void createNewDocument() {
        Dispatch documents = Dispatch.get(MsWordApp, "Documents").toDispatch();
        document = Dispatch.call(documents, "Add").toDispatch();
    }

    // 打开一个存在的word文档,并用document 引用 引用它
    public void openFile(String wordFilePath) {
        Dispatch documents = Dispatch.get(MsWordApp, "Documents").toDispatch();
        document = Dispatch.call(documents, "Open", wordFilePath,
                new Variant(true)/* 是否进行转换ConfirmConversions */,
                new Variant(false)/* 是否只读 */).toDispatch();
    }

    //向文档中插入题目
    public void insertTheme(String text) {
        moveRight(1);
        Dispatch font = Dispatch.get(selection, "Font").toDispatch();
        Dispatch.put(font, "Name", new Variant("宋体")); //
        Dispatch.put(font, "Size", new Variant(24)); // 小四
        Dispatch alignment = Dispatch.get(selection, "ParagraphFormat")
                .toDispatch();// 段落格式
        Dispatch.put(alignment, "Alignment", "1"); // (1:置中 2:靠右 3:靠左)

        Dispatch.put(selection, "Text", text);
        // 取消选中,应该就是移动光标
        moveRight(1);

        font = Dispatch.get(selection, "Font").toDispatch();
        //Dispatch.put(font, "Bold", new Variant(true)); // 设置为黑体
        //Dispatch.put(font, "Italic", new Variant(true)); // 设置为斜体
        Dispatch.put(font, "Name", new Variant("宋体")); //
        Dispatch.put(font, "Size", new Variant(12)); // 小四
        //selection = Dispatch.get(MsWordApp, "Selection").toDispatch();
        //Dispatch.call(selection, "TypeParagraph");// 插入一个空行
        alignment = Dispatch.get(selection, "ParagraphFormat")
                .toDispatch();// 段落格式
        Dispatch.put(alignment, "Alignment", "3"); // (1:置中 2:靠右 3:靠左)
    }

    // 向 document 中插入文本内容
    public void insertText(String textToInsert) {
        // 获得选 中的内容，如果是一个新创建的文件，因里面无内容，则光标应处于文件开头处
        selection = Dispatch.get(MsWordApp, "Selection").toDispatch();
        moveRight(1);
        Dispatch.put(selection, "Text", textToInsert);
        // 取消选中,应该就是移动光标
        moveRight(1);
    }

    // 向文档中添加 一个图片，
    public void insertJpeg(String jpegFilePath) {
        selection = Dispatch.get(MsWordApp, "Selection").toDispatch();

        Dispatch image = Dispatch.get(selection, "InLineShapes").toDispatch();
        Dispatch.call(image, "AddPicture", jpegFilePath);
    }

    // 段落的处理,插入格式化的文本
    public void insertFormatStr(String text) {
        Dispatch wordContent = Dispatch.get(document, "Content").toDispatch(); // 取得word文件的内容
        Dispatch.call(wordContent, "InsertAfter", text);// 插入一个段落到最后
        Dispatch paragraphs = Dispatch.get(wordContent, "Paragraphs")
                .toDispatch(); // 所有段落
        int paragraphCount = Dispatch.get(paragraphs, "Count").changeType(
                Variant.VariantInt).getInt();// 一共的段落数

        // 找到刚输入的段落，设置格式
        Dispatch lastParagraph = Dispatch.call(paragraphs, "Item",
                new Variant(paragraphCount)).toDispatch(); // 最后一段（也就是刚插入的）
        // Range 对象表示文档中的一个连续范围，由一个起始字符位置和一个终止字符位置定义
        Dispatch lastParagraphRange = Dispatch.get(lastParagraph, "Range")
                .toDispatch();

        Dispatch font = Dispatch.get(lastParagraphRange, "Font").toDispatch();
        Dispatch.put(font, "Bold", new Variant(true)); // 设置为黑体
        Dispatch.put(font, "Italic", new Variant(true)); // 设置为斜体
        Dispatch.put(font, "Name", new Variant("宋体")); //
        Dispatch.put(font, "Size", new Variant(12)); // 小四

        selection = Dispatch.get(MsWordApp, "Selection").toDispatch();
        Dispatch.call(selection, "TypeParagraph");// 插入一个空行
        Dispatch alignment = Dispatch.get(selection, "ParagraphFormat")
                .toDispatch();// 段落格式
        Dispatch.put(alignment, "Alignment", "1"); // (1:置中 2:靠右 3:靠左)
    }

    public void typeParagraph()
    {
        selection = Dispatch.get(MsWordApp,"Selection").toDispatch();
        Dispatch.call(selection, "TypeParagraph");
    }
    // word 中在对表格进行遍历的时候 ，是先列后行 先column 后cell
    // 另外下标从1开始
    public void insertTable(String tableTitle, int row, int column) {
        selection = Dispatch.get(MsWordApp, "Selection").toDispatch(); // 输入内容需要的对象
        Dispatch.call(selection, "TypeText", tableTitle); // 写入标题内容 // 标题格行
        Dispatch.call(selection, "TypeParagraph"); // 空一行段落
        Dispatch.call(selection, "TypeParagraph"); // 空一行段落
        Dispatch.call(selection, "MoveDown"); // 游标往下一行

        // 建立表格
        Dispatch tables = Dispatch.get(document, "Tables").toDispatch();
        // int count = Dispatch.get(tables,
        // "Count").changeType(Variant.VariantInt).getInt(); // document中的表格数量
        // Dispatch table = Dispatch.call(tables, "Item", new Variant(
        // 1)).toDispatch();//文档中第一个表格
        Dispatch range = Dispatch.get(selection, "Range").toDispatch();// /当前光标位置或者选中的区域

        Dispatch newTable = Dispatch.call(tables, "Add", range,
                new Variant(row), new Variant(column), new Variant(1))
                .toDispatch(); // 设置row,column,表格外框宽度
        Dispatch cols = Dispatch.get(newTable, "Columns").toDispatch(); // 此表的所有列，
        int colCount = Dispatch.get(cols, "Count").changeType(
                Variant.VariantInt).getInt();// 一共有多少列 实际上这个数==column

        for (int i = 1; i <= colCount; i++) { // 循环取出每一列
            Dispatch col = Dispatch.call(cols, "Item", new Variant(i))
                    .toDispatch();
            Dispatch cells = Dispatch.get(col, "Cells").toDispatch();// 当前列中单元格
            int cellCount = Dispatch.get(cells, "Count").changeType(
                    Variant.VariantInt).getInt();// 当前列中单元格数 实际上这个数等于row

            for (int j = 1; j <= cellCount; j++) {// 每一列中的单元格数
                putTxtToCell(newTable, j, i, "第" + j + "行，第" + i + "列");// 与上面四句的作用相同
            }

        }

    }

    /** */
    /**
     * 在指定的单元格里填写数据
     *
     * @param tableIndex
     * @param cellRowIdx
     * @param cellColIdx
     * @param txt
     */
    public void putTxtToCell(Dispatch table, int cellRowIdx, int cellColIdx,
                             String txt) {
        Dispatch cell = Dispatch.call(table, "Cell", new Variant(cellRowIdx),
                new Variant(cellColIdx)).toDispatch();
        Dispatch.call(cell, "Select");
        selection = Dispatch.get(MsWordApp, "Selection").toDispatch(); // 输入内容需要的对象
        Dispatch.put(selection, "Text", txt);
    }

    /** */
    /**
     * 在指定的单元格里填写数据
     *
     * @param tableIndex
     * @param cellRowIdx
     * @param cellColIdx
     * @param txt
     */
    public void putTxtToCell(int tableIndex, int cellRowIdx, int cellColIdx,
                             String txt) {
        // 所有表格
        Dispatch tables = Dispatch.get(document, "Tables").toDispatch();
        // 要填充的表格
        Dispatch table = Dispatch.call(tables, "Item", new Variant(tableIndex))
                .toDispatch();
        Dispatch cell = Dispatch.call(table, "Cell", new Variant(cellRowIdx),
                new Variant(cellColIdx)).toDispatch();
        Dispatch.call(cell, "Select");
        selection = Dispatch.get(MsWordApp, "Selection").toDispatch(); // 输入内容需要的对象
        Dispatch.put(selection, "Text", txt);
    }

    // 合并两个单元格
    public void mergeCell(Dispatch cell1, Dispatch cell2) {
        Dispatch.call(cell1, "Merge", cell2);
    }

    public void mergeCell(Dispatch table, int row1, int col1, int row2, int col2) {
        Dispatch cell1 = Dispatch.call(table, "Cell", new Variant(row1),
                new Variant(col1)).toDispatch();
        Dispatch cell2 = Dispatch.call(table, "Cell", new Variant(row2),
                new Variant(col2)).toDispatch();
        mergeCell(cell1, cell2);
    }

    public void mergeCellTest() {
        Dispatch tables = Dispatch.get(document, "Tables").toDispatch();
        int tableCount = Dispatch.get(tables, "Count").changeType(
                Variant.VariantInt).getInt(); // document中的表格数量
        Dispatch table = Dispatch.call(tables, "Item", new Variant(tableCount))
                .toDispatch();// 文档中最后一个table
        mergeCell(table, 1, 1, 1, 2);// 将table 中x=1,y=1 与x=1,y=2的两个单元格合并
    }

    // ========================================================

    /** */
    /**
     * 把选定的内容或光标插入点向上移动
     *
     * @param pos
     *            移动的距离
     */
    public void moveUp(int pos) {
        selection = Dispatch.get(MsWordApp, "Selection").toDispatch(); // 输入内容需要的对象
        for (int i = 0; i < pos; i++) {
            Dispatch.call(selection, "MoveUp");
        }
    }


    public void moveDown(int pos) {
        if (selection == null)
            selection = Dispatch.get(MsWordApp, "Selection").toDispatch();
        for (int i = 0; i < pos; i++)
            Dispatch.call(selection, "MoveDown");
    }
    /** *//**
     * 把选定的内容或者插入点向左移动 
     *
     * @param pos 移动的距离 
     */
    public void moveLeft(int pos) {
        if (selection == null)
            selection = Dispatch.get(MsWordApp, "Selection").toDispatch();
        for (int i = 0; i < pos; i++) {
            Dispatch.call(selection, "MoveLeft");
        }
    }

    /** *//**
     * 把选定的内容或者插入点向右移动 
     *
     * @param pos 移动的距离 
     */
    public void moveRight(int pos)

    {
        if (selection == null)
            selection = Dispatch.get(MsWordApp, "Selection").toDispatch();
        for (int i = 0;i <  pos; i++)
            Dispatch.call(selection, "MoveRight");
    }

    public void moveStart()
    {
        if(selection == null)
            selection = Dispatch.get(MsWordApp, "selection").toDispatch();
        Dispatch.call(selection, "HomeKey", new Variant(6));
    }

    public void moveEnd()
    {
        if(selection == null)
            selection = Dispatch.get(MsWordApp, "selection").toDispatch();
        Dispatch.call(selection, "EndKey", new Variant(6));
    }
    /** */
    /**
     * 从选定内容或插入点开始查找文本
     *
     * @param toFindText
     *            要查找的文本
     * @return boolean true-查找到并选中该文本，false-未查找到文本
     */
    public boolean find(String toFindText) {
        boolean returned = false;
        if (toFindText == null || toFindText.equals(""))
            return returned;
        selection = Dispatch.get(MsWordApp, "Selection").toDispatch(); // 输入内容需要的对象
        // 从selection所在位置开始查询
        Dispatch find = Dispatch.call(selection, "Find").toDispatch();
        // 设置要查找的内容
        Dispatch.put(find, "Text", toFindText);
        // 向前查找
        Dispatch.put(find, "Forward", "True");
        // 设置格式
        Dispatch.put(find, "Format", "True");
        // 大小写匹配
        Dispatch.put(find, "MatchCase", "True");
        // 全字匹配
        Dispatch.put(find, "MatchWholeWord", "True");
        // 查找并选中
        returned = Dispatch.call(find, "Execute").getBoolean();
        return returned;
    }

    /** */
    /**
     * 把选定选定内容设定为替换文本
     *
     * @param toFindText
     *            查找字符串
     * @param newText
     *            要替换的内容
     * @return
     */
    public boolean replaceText(String toFindText, String newText) {
        if (!find(toFindText))
            return false;
        selection = Dispatch.get(MsWordApp, "Selection").toDispatch(); // 输入内容需要的对象
        Dispatch.put(selection, "Text", newText);
        return true;
    }

    public void printFile() {
        // Just print the current document to the default printer
        Dispatch.call(document, "PrintOut");
    }

    // 保存文档的更改
    public void save() {
        Dispatch.call(document, "Save");
    }

    public void saveFileAs(String filename) {
        Dispatch.call(document, "SaveAs", filename);
    }

    public void closeDocument() {
        Dispatch.call(document, "Close");
        document = null;
    }

    public void closeWord() {
        Dispatch.call(MsWordApp, "Quit");
        MsWordApp = null;
        document = null;
    }

    // 设置wordApp打开后窗口的位置
    public void setLocation() {
        Dispatch activeWindow = Dispatch.get(MsWordApp, "Application")
                .toDispatch();
        Dispatch.put(activeWindow, "WindowState", new Variant(1)); // 0=default
        // 1=maximize
        // 2=minimize
        Dispatch.put(activeWindow, "Top", new Variant(0));
        Dispatch.put(activeWindow, "Left", new Variant(0));
        Dispatch.put(activeWindow, "Height", new Variant(600));
        Dispatch.put(activeWindow, "width", new Variant(800));
    }

    public synchronized static EventInfo loadWordBean(String evtID,List<BarChartInfo> barCharInfos)
    {
        String sql = null;
        EventInfo wb = new EventInfo();
        try{
            sql = "select * from eventinfo where eventID='"+evtID+"'";
            wb = DBConnect.excuteReadOneRow(EventInfo.class, sql);
        }catch(Exception e)
        {
            MDC.put("eventID",evtID);
            LogWriter.logger.warn("加载word信息异常");
            e.printStackTrace();
        }
        return wb;

    }

    public void setWord(EventInfo wb)
    {
        selection = Dispatch.get(MsWordApp, "Selection").toDispatch();
        insertTheme("国际强震信息统计报告");
        typeParagraph();
        insertText("事件ID：" + wb.getEventID());
        typeParagraph();
        insertText("事件时间：" + wb.getEventTime());
        typeParagraph();
        if(wb.getEventLocation()==null||wb.getEventLocation().isEmpty())
            insertText("事件地点：" + wb.getEventName());
        else
            insertText("事件地点：" + wb.getEventName()+", "+wb.getEventLocation());
        typeParagraph();
        insertText("地震震级：" + wb.getMagnitude());
        typeParagraph();
        if(wb.getDeathInfo() == null)

            insertText("死亡情况：" + "暂无信息");
        else
            insertText("死亡情况：" + wb.getDeathInfo());
        typeParagraph();
        if(wb.getInjureInfo() == null)
            insertText("受伤情况：" + "暂无信息");
        else
            insertText("受伤情况：" + wb.getInjureInfo());
        typeParagraph();
        if(wb.getBuildingInfo() == null)
        {
            insertText("建筑物损伤情况：暂无信息");
        }
        else
            insertText("建筑物损伤情况：" + wb.getBuildingInfo());
        typeParagraph();
        if(wb.getUrl() == null)
            insertText("相关网站：暂无信息");
        else
            insertText("相关网站：" + wb.getUrl());
        typeParagraph();
        if(wb.getWebName() == null)
            insertText("网站名称：暂无信息");
        else
            insertText("网站名称：" + wb.getWebName());
        typeParagraph();
        if(wb.getDeathUpdateTime() == null)
            insertText("伤亡情况更新时间：暂无信息");
        else
            insertText("伤亡情况更新时间：" + wb.getDeathUpdateTime());
        typeParagraph();


        insertText("舆情新闻发布趋势图：");

        typeParagraph();

        if(new File(jpegPosition + "line" + wb.getEventID() + ".png").exists())
            insertJpeg(jpegPosition + "line" + wb.getEventID() + ".png");
        else
            insertText("暂无舆情新闻发布趋势信息!");
        typeParagraph();
        insertText("时序统计分析图：");
        typeParagraph();
        if(new File(jpegPosition + wb.getEventID() + ".png").exists())
        {
            System.out.println("插入图片成功");
            insertJpeg(jpegPosition + wb.getEventID() + ".png");
            typeParagraph();

            if(wb.getBarCharInfos() != null)
            {
                insertTable("高频词", wb.getBarCharInfos().size() + 1, 4);
                putTxtToCell(1, 1, 1, "高频词");
                putTxtToCell(1, 1, 2, "时序统计的时间分割范围");
                putTxtToCell(1, 1, 3, "词频计数");
                putTxtToCell(1, 1, 4, "代表文本链接地址");


                for(int i = 0; i < wb.getBarCharInfos().size(); i++)
                {
                    BarChartInfo barChartInfo = wb.getBarCharInfos().get(i);
                    String word = barChartInfo.getWord();
                    ArrayList<String> timeScope = barChartInfo.getxTime();
                    ArrayList<String> wordTimeScopeCount = barChartInfo.getyCount();
                    ArrayList<String> timeScopeWebName = barChartInfo.getWebNameDisplay();
                    String time = timeScope.toString();
                    String wordtimecount = wordTimeScopeCount.toString();
                    String webName = timeScopeWebName.toString();
                    time = time.substring(1, time.length()-1);
                    wordtimecount = wordtimecount.substring(1, wordtimecount.length()-1);
                    webName = webName.substring(1, webName.length()-1);
                    int k = i + 2;
                    putTxtToCell(1, k, 1, word );
                    putTxtToCell(1, k, 2, time);
                    putTxtToCell(1, k, 3, wordtimecount);
                    putTxtToCell(1, k, 4, webName);


//    				}
                }
            }
        }
        else
        {
            insertText("暂无时序统计分析信息!");
            typeParagraph();
        }


    }

    public static void createANewFile(EventInfo wb) {

        GenerateWord generateWord = new GenerateWord();
        generateWord.setVisible(false);
        generateWord.createNewDocument();// 创建一个新文档
        generateWord.setLocation();// 设置打开后窗口的位置
        generateWord.setWord(wb);
        // 如果 ，想保存文件，下面三句
        System.out.println("doc 文件路径为："+docPosition+wb.getEventID());
        generateWord.saveFileAs( docPosition + wb.getEventID() + ".doc");
        System.out.println("新建word文档成功！");
        generateWord.closeDocument();
        generateWord.closeWord();
    }

    public static void main(String[] args){

    }
}

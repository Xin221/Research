package inetintelliprocess.searchengine.searchers.dailysearcher;
/**
 * dailysearcher日常搜索核心包
 */
import inetintelliprocess.searchengine.frame.DailyManager;
import inetintelliprocess.searchengine.searchers.TargetLink;
import inetintelliprocess.searchengine.searchers.WebPageInfo;
import inetintelliprocess.searchengine.templates.SearcherTemplate;
import inetintelliprocess.util.LogWriter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.MDC;
import org.w3c.dom.Element;

public class DailyCoreSearcher extends DailySearcher {

    private String baseURI = "";

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }
    public DailyCoreSearcher() {

    }

    public DailyCoreSearcher(DailyManager p) {
        super(p);
    }

    /**
     * 初始化一个网站线程
     * @param template
     * @return
     */
    public Boolean init(Element template) {//
        if (template == null)
            return false;
        this.setMyTemplate(new SearcherTemplate());//设置搜索模板
        if (!this.getMyTemplate().loadFromXml(template))//加载搜索模板xml
            return false;
        setBaseURI(this.getMyTemplate().getBaseURI());//例如xinhuanet.com
        return true;
    }

    public void run() {
        run_search();
    }

    public void run_search() {
        String uri = this.getMyTemplate().getBaseURI();
        try {
            List<TargetLink> result = new ArrayList<TargetLink>();
            //int tt=1;
            while(result==null||result.size()==0){
                result = getLink(uri, "");
                if (result != null && result.size() > 0) {
                    break;
                }
            }

            if (result != null && result.size() > 0) {
                // 清空链接缓存
                cache.clear();
                // 设置搜索深度的初始值
                // 将所有的相关链接加入链接缓存
                for (int i = 0; i < result.size(); i++) {
                    if(result.get(i).getLinkURL().contains("http://")&&!result.get(i).getLinkURL().contains(uri))
                        continue;
                    result.get(i).setDepth(1);
                    cache.add(result.get(i));
                }
                while (running && cache.size() > 0 ) {
                    // 提取网页信息
                    if (!searchWebPages())//参数count为新加入的，此处有修改！！！！！！！！！！！！
                        break;
                    // 搜索任务完成跳出，从新开始搜索 ;
                    if (cache.size() <= 0) {
                        break;
                    } else {
                        System.out.println(uri + "继续搜索");

                    }
                    //////////////////////////////////start
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }	//
                    /////////////////////////////////end
                }
            } else {
                System.out.println("未发现新链接，退出执行。");
            }
        } catch (Exception exp) {
//			LogWriter.logger.error(exp);
            exp.printStackTrace();
        }
		/*running = false;*/
        System.out.println("***" + uri + "搜索完毕，退出线程");
        MDC.put("eventID", "日常搜索");
        LogWriter.logger.info(uri + "搜索完毕，退出线程");
    }

    // 提取网页信息

    public boolean searchWebPages() {
        boolean flag = true;
        try {
            if (cache.size() <= 0 )
                flag = false;

            while( cache.size() > 0){

                TargetLink tlink = cache.removeFirst();
                if(tlink != null){
                    if (tlink.getSearched())
                        continue;
                    tlink.setSearched(true);
                    String url = tlink.getLinkURL();
                    String validUri = validUri(url);
                    if (!url.startsWith("http")) {
                        if (!url.startsWith("http://www"))
                            if(url.contains(this.getBaseURI()))
                                url = "http://www." + url;
                            else if(url.contains("/"))
                                url = "http://www." + this.getBaseURI() + url;
                            else
                                continue;
                        else if(!url.contains(this.getBaseURI()))
                            continue;
                    }
                    WebPageInfo info = getContent(url, this.getMyTemplate()
                            .getTagName(), this.getMyTemplate().getAttName(), this
                            .getMyTemplate().getValue());
                    // 如果存在信息

                    if (info != null) {
                        if (isValidPage(info)) {
                            String tname = this.getTblName();
                            ////////////////////////////////////////////start
                            info.setWebName(url);
                            if(info.getWebName().equals("腾讯网")||info.getWebName().equals("其他网站")||info.getWebName().contains("RSS"))
                                info.setWebType("generalWeb");
                            else
                                info.setWebType("coreWeb");
                            /////////////////////////////////////////////end
                            info.setPageContent(info.getPageContent().replaceAll(
                                    this.getMyTemplate().getName() + "网", ""));
                            info.setPageTitle(info.getPageTitle().replaceAll(
                                    this.getMyTemplate().getName() + "网", ""));
                            info.setPageContent(info.getPageContent().replaceAll(
                                    this.getMyTemplate().getName(), ""));
                            info.setPageTitle(info.getPageTitle().replaceAll(
                                    this.getMyTemplate().getName(), ""));
                            ///////////////////////////////////////////////////start
//							if(DbTools.querySumItem("generalwebinfotbl") >= MAX_ITEM)
//								DbTools.clearPartInfo("generalwebinfotbl","pageTime",delNum);
                            info.write(tname, info);
                            //////////////////////////////////////////////end
                            System.out.println("写入数据表" + tname + ":"
                                    + info.getPageTitle());
                        }
                    }

                    // 从tlink页面抽新的 link
                    List<TargetLink> currentPageLinkResult = getLink(
                            tlink.getLinkURL(), validUri);
                    String currentPageURI = getParentPath(tlink.getLinkURL());
                    int currentLinkSeperator = tlink.getDepth();
                    if (currentPageLinkResult != null) {
                        for (int k = 0; k < currentPageLinkResult.size(); k++) {
                            TargetLink newLink = currentPageLinkResult.get(k);
                            if (newLink.getLinkURL().startsWith(".")
                                    || newLink.getLinkURL().startsWith("/")
                                    || !newLink.getLinkURL().startsWith("http:")) {
                                newLink.setLinkURL(getValidURL(currentPageURI,
                                        newLink.getLinkURL()));
                            }
                            // 设置新链接的搜索深度
                            newLink.setDepth(tlink.getDepth() + 1);
                            if (newLink.getLinkURL().indexOf(
                                    this.getMyTemplate().getBaseURI()) < 0) {// 非本站内的url
                                // 放在公共里面
                                if(!this.getParentMgr().getGeneralSearcher().cache.contains(newLink))
                                    this.getParentMgr().getGeneralSearcher().cache.add(newLink);
                            } else {
                                // 本站内的，比较级数
                                //int Seperator = countChar(newLink.getLinkURL(), "/");
                                int Seperator = newLink.getDepth();
                                if (Seperator < currentLinkSeperator)
                                    continue;
                                if(!cache.contains(newLink))
                                    cache.add(newLink);
                            }
                            //count++;
                        }// for
                        currentPageLinkResult.clear();
                        currentPageLinkResult = null;
                    }//newtLinks
                }
            }
        }catch (Exception exp) {
            LogWriter.logger.error(exp);
            exp.printStackTrace();
        }
        return flag;
    }

    // 判断信息是否有效
    public boolean isValidPage(WebPageInfo info) {
        if (info == null)
            return false;
        int count = 0;
        for (int j = 0; j < this.getParentMgr().getKwords().size(); j++) {
            String name = this.getParentMgr().getKwords().get(j).getWord();
            if (info.getPageContent().toLowerCase().contains(name.toLowerCase())) {
                count++;
            }
        }
        int hitcount = this.getParentMgr().getKwords().size() / 3;
        if (count - 1 >= hitcount && info.getPageTitle()!=null && info.getPageTitle().length()>=18)
            return true;
        return false;
    }

    public static void main(String[] args) {

        try {
            DailyCoreSearcher srh = new DailyCoreSearcher();
            Thread Demo = new Thread(srh);
            Demo.start();
        } catch (Exception e) {
            LogWriter.logger.error(e);
            e.printStackTrace();
        }
    }
}

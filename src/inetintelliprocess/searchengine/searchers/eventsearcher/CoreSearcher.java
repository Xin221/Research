package inetintelliprocess.searchengine.searchers.eventsearcher;
/**
 * eventsearcher事件搜索核心包
 */
import inetintelliprocess.searchengine.frame.SearcherManager;
import inetintelliprocess.searchengine.searchers.TargetLink;
import inetintelliprocess.searchengine.searchers.WebPageInfo;
import inetintelliprocess.searchengine.templates.SearcherTemplate;
import inetintelliprocess.util.LogWriter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.MDC;
import org.w3c.dom.Element;

/**
 *
 * 核心网站搜索的业务类
 * 主要负责核心网站信息的搜索
 *
 */
public class CoreSearcher extends Searcher {

    private String baseURI = "";

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }
    public CoreSearcher() {

    }

    public CoreSearcher(SearcherManager p) {
        super(p);//parentMgr = p;
    }

    /**
     * 初始化
     * @param template
     * @return
     */
    public Boolean init(Element template) {//
        if (template == null)
            return false;
        this.setMyTemplate(new SearcherTemplate());//设置搜索模板
        if (!this.getMyTemplate().loadFromXml(template))//加载搜索模板中一个coreWeb结点的子节点内容
            return false;
        setBaseURI(this.getMyTemplate().getBaseURI());//设置首页
        return true;
    }

    public void run() {
        run_search();
    }

    /**
     * 从搜索队列中获取URL，判断搜索深度，调用searchWebPages方法
     */
    public void run_search() {
        String uri = this.getMyTemplate().getBaseURI();//首页xinhuanet.cn
        try {
            List<TargetLink> result = new ArrayList<TargetLink>();

            //
            for (int count = 0; count < 5 && running; count++) {
                result = getLink(uri, "");//获取网页中与事件name相关的url
                if (result != null && result.size() > 0) {
                    break;
                }
                Thread.sleep(1000);
            }

            if (result != null && result.size() > 0) {
                // 清空链接缓存
                cache.clear();
                for (int i = 0; i < result.size(); i++) {
                    result.get(i).setDepth(1);
                    cache.add(result.get(i));// 将所有的相关链接加入链接缓存

                }
                while (running && cache.size() > 0) {
                    if (!searchWebPages())// 提取网页信息
                        break;
                    // 搜索任务完成跳出，从新开始搜索 ;
                    if (cache.size() <= 0) {
						    /*System.out.println("***" + uri + "完成搜索，开始新一轮搜索");
						    LogWriter.logger.info(uri + "完成搜索，开始新一轮搜索");*/
                        break;
                    } else {
                        System.out.println(uri + "继续搜索");
                        MDC.put("eventID", this.getMyTemplate().getID());
                        LogWriter.logger.info(uri + "继续搜索");
                    }
                }
            } else {
                System.out.println("未发现新链接，退出执行。");
            }

        } catch (Exception exp) {
            LogWriter.logger.error(exp);
            exp.printStackTrace();
        }
        running = false;
        System.out.println("***" + uri + "搜索完毕，退出线程");
        LogWriter.logger.info(uri + "搜索完毕，退出线程");
    }

    /**
     *  提取网页信息，并写入相应的文本对象库表中
     *  提取URL，存入相应的搜索队列中
     * @return
     */
    public boolean searchWebPages() {
        int count = 0;
        boolean flag = true;
        try {
            if (cache.size() <= 0 )
                flag = false;
            while( cache.size()> 0){
                TargetLink tlink = cache.removeFirst();//取一个
                if(tlink != null && tlink.getDepth() < this.upperDepth){
                    if (tlink.getSearched())//已经搜索过了则跳过
                        continue;
                    tlink.setSearched(true);//置是否搜索
                    String url = tlink.getLinkURL();//获取url
                    String validUri = validUri(url);//提取有效uri
                    if (!url.startsWith("http")) {
                        if (!url.startsWith("http://www"))//不是以http且http://www开头的则加上
                            url = "http://www." + url;
                    }
                    //提取指定url中的tag标签网页信息and值为tag_value的attName标签网页信息
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
                                    this.getMyTemplate().getName() + "网", ""));//pageContent
                            info.setPageTitle(info.getPageTitle().replaceAll(
                                    this.getMyTemplate().getName() + "网", ""));//pageTitle
                            info.setPageContent(info.getPageContent().replaceAll(
                                    this.getMyTemplate().getName(), ""));
                            info.setPageTitle(info.getPageTitle().replaceAll(
                                    this.getMyTemplate().getName(), ""));
                            info.write(tname, info);//写入数据表
                            // 写入之前首先看url是否存在当前数据库表（tname）
                            // 把当前信息写入对应的当前数据库表（tname）中
                            System.out.println("写入数据表" + tname + ":"
                                    + info.getPageTitle());
                        } else
                            continue;
                    }

                    // 从tlink页面抽新的与事件相关的 link
                    List<TargetLink> currentPageLinkResult = getLink(
                            tlink.getLinkURL(), validUri);
                    if (currentPageLinkResult != null) {
                        //获得父节点路径
                        // http://www.w3.org/1999/xhtml
                        //获得http://www.w3.org/1999/
                        String currentPageURI = getParentPath(tlink.getLinkURL());
                        //计算url中有几个“/”子串，得到的即当前搜索深度
                        //int currentLinkSeperator = countChar(tlink.getLinkURL(), "/");
                        int currentLinkSeperator = tlink.getDepth();
                        for (int k = 0; k < currentPageLinkResult.size(); k++) {
                            TargetLink newLink = currentPageLinkResult.get(k);
                            //如果是以“.”或“/”或者不是以“http:”开头的，则找关于它的有效链接
                            if (newLink.getLinkURL().startsWith(".")
                                    || newLink.getLinkURL().startsWith("/")
                                    || !newLink.getLinkURL().startsWith("http:")) {
                                newLink.setLinkURL(getValidURL(currentPageURI,
                                        newLink.getLinkURL()));
                            }
                            // 设置新链接的搜索深度
                            newLink.setDepth(tlink.getDepth() + 1);
                            if(newLink.getDepth() < this.upperDepth && count < MAX_DEPTH){
                                if (newLink.getLinkURL().indexOf(
                                        this.getMyTemplate().getBaseURI()) < 0) {// 非本站内的url
                                    // 放在普通搜索里面
                                    this.getParentMgr().getGeneralSearcher()
                                            .cache.add(newLink);
                                } else {
                                    // 本站内的，比较级数，属于本级或更深层的放进cache
                                    //int Seperator = countChar(newLink.getLinkURL(), "/");
                                    int Seperator = newLink.getDepth();
                                    if (Seperator < currentLinkSeperator)
                                        continue;
                                    cache.add(newLink);
                                }
                                count++;
                            }
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

    /**
     *  判断网页信息是否有效，网页内容是否有关键词
     * @param info
     * @return
     */
    //2014年1月15日修改版本
    public boolean isValidPage(WebPageInfo info) {
        if (info == null)
            return false;
        int hitAddrCount = 0;
        //判断是否与事件相关，比较地点关键字
        for (int j = 0; j < this.getParentMgr().getAddrwords().size(); j++) {
            String name = this.getParentMgr().getAddrwords().get(j).getWord();
            if (info.getPageContent().toLowerCase().contains(name.toLowerCase())) {
                hitAddrCount++;
            }
        }
        //是否有一半以上的地点关键字匹配，匹配一半以上的地点关键字则为有效，否则如果地点关键字和预设关键字各至少有一个匹配则为有效
        int hitcount = this.getParentMgr().getAddrwords().size() / 2;
        if(info.getPageTitle() != null && !info.getPageTitle().isEmpty())
            if (hitAddrCount - 1 >= hitcount)
                return true;
            else if(hitAddrCount >= 1){
                for(int i = 0; i < this.getParentMgr().getKwords().size(); i++) {
                    String key = this.getParentMgr().getKwords().get(i).getWord();
                    if(info.getPageContent().toLowerCase().contains(key.toLowerCase())){
                        return true;
                    }
                }
            }
        return false;
    }

    //2014年1月15日之前的版本
//	public boolean isValidPage(WebPageInfo info) {
//		if (info == null)
//			return false;
//		int count = 0;
//		//判断是否与事件相关，比较关键字
//		for (int j = 0; j < this.getParentMgr().getKwords().size(); j++) {
//			String name = this.getParentMgr().getKwords().get(j).getWord();
//			if (info.getPageContent().toLowerCase().contains(name.toLowerCase())) {
//				count++;
//			}
//		}
//		//是否有一半以上的关键字，匹配一半以上的关键字则为有效
//		int hitcount = this.getParentMgr().getKwords().size() / 3;
//		if (count - 1 >= hitcount && info.getPageTitle()!=null && info.getPageTitle().length()>=18)
//			return true;
//		return false;
//	}

    public static void main(String[] args) {

        try {
            CoreSearcher srh = new CoreSearcher();
            Thread Demo = new Thread(srh);
            Demo.start();
        } catch (Exception e) {
            LogWriter.logger.error(e);
            e.printStackTrace();
        }
    }
}

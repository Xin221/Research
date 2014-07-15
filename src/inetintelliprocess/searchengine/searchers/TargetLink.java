package inetintelliprocess.searchengine.searchers;
/**
 * searchers搜索核心包
 * 搜索实现
 */
/**
 * 要访问的一个链接的信息
 * 搜索层的业务类
 * 主要负责定义相关链接的信息
 * 包括URL地址、搜索深度、链接锚、是否已被搜索的标志位
 */
public class TargetLink {
    private String ID = "";
    private String linkURL = "";
    private String defLinkURL = "http://news.qq.com/a/20130319/000888.htm";
    private String title = "";
    private int depth = 0;
    private Boolean searched = false;
    public String getDefLinkURL() {
        return defLinkURL;
    }
    public void setDefLinkURL(String defLinkURL) {
        this.defLinkURL = defLinkURL;
    }
    public String getID() {
        return ID;
    }
    public void setID(String iD) {
        ID = iD;
    }
    public String getLinkURL() {
        return linkURL;
    }
    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = depth;
    }
    public Boolean getSearched() {
        return searched;
    }
    public void setSearched(Boolean searched) {
        this.searched = searched;
    }
}

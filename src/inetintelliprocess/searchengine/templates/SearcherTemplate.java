package inetintelliprocess.searchengine.templates;
/**
 * 信息搜索模板
 */
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 *
 * 信息搜索模板管理的业务类
 * 主要负责搜索模板的定义、修改
 * test.xml
 *
 */
public class SearcherTemplate {

    private String ID = "";
    private String name = "";
    private String baseURI = "";
    private String tagName = "";
    private String attName = "";
    private String value = "";

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        ID = iD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getAttName() {
        return attName;
    }

    public void setAttName(String attName) {
        this.attName = attName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 加载搜索模板中一个coreWeb结点的子节点内容
     * @param node
     * @return
     */
    public Boolean loadFromXml(Element node) {
        if (node == null)
            return false;
        if (!node.getNodeName().equals("URL"))
            return false;
        this.name = node.getAttribute("name");//属性
        NodeList clist = node.getChildNodes();//子节点
        for (int i = 0; i < clist.getLength(); i++) {
            Node cn = clist.item(i);
            if (cn == null)
                continue;
            if (cn.getNodeName().equals("URL_value")) {
                this.baseURI = cn.getTextContent();
            } else if (cn.getNodeName().equals("tag")) {
                Element cne = (Element) (cn);
                this.tagName = cne.getAttribute("name");
                this.attName = cne.getAttribute("attname");
                this.value = cne.getTextContent();
            }
        }
        return true;
    }
}

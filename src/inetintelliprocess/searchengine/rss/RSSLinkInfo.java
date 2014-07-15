package inetintelliprocess.searchengine.rss;

public class RSSLinkInfo {
    private String ID = null;
    private String rssLinkURL = null;
    private String rssWebType = null;
    private String rssWebName = null;
    private String rssExtractType = null;

    public String getID() {
        return ID;
    }

    public String getRssLinkURL() {
        return rssLinkURL;
    }

    public String getRssWebType() {
        return rssWebType;
    }

    public String getRssWebName() {
        return rssWebName;
    }

    public String getRssExtractType() {
        return rssExtractType;
    }

    public void setID(String iD) {
        ID = iD;
    }

    public void setRssLinkURL(String rssLinkURL) {
        this.rssLinkURL = rssLinkURL;
    }

    public void setRssWebType(String rssWebType) {
        this.rssWebType = rssWebType;
    }

    public void setRssWebName(String rssWebName) {
        this.rssWebName = rssWebName;
    }

    public void setRssExtractType(String rssExtractType) {
        this.rssExtractType = rssExtractType;
    }

	
	/*public static void main(String[] args) {
		ArrayList<RSSLinkInfo> rssinfos = new ArrayList<RSSLinkInfo>();
		RSSDBO rssdbo = new RSSDBO();
		rssinfos = rssdbo.loadRSSInfos();
		for(RSSLinkInfo rssinfo : rssinfos) {
			System.out.println(rssinfo.ID);
			System.out.println(rssinfo.rssLinkURL);
			System.out.println(rssinfo.rssWebType);
			System.out.println(rssinfo.rssWebName);
			System.out.println(rssinfo.rssExtractType);
			System.out.println("***************************************************");
		}
	}*/
}

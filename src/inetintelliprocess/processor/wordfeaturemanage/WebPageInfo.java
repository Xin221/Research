package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.processor.wordfeaturemanage.WordVector.wordCount;

public class WebPageInfo {

    private String pageId;
    private String pageTitle;
    private String pageTime;
    private String lastWriteTime;
    private String pageContent;
    private String url;
    private String Abstract;
    private String webName;

    private WordVector myVec = null ;

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId){
        this.pageId = pageId;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle){
        this.pageTitle = pageTitle;
    }

    public String getPageTime() {
        return pageTime;
    }

    public void setPageTime(String pageTime){
        this.pageTime = pageTime;
    }

    public String getLastWriteTime() {
        return lastWriteTime;
    }

    public void setLastWriteTime(String lastWriteTime){
        this.lastWriteTime = lastWriteTime;
    }

    public String getPageContent() {
        return pageContent;
    }

    public void setPageContent(String pageContent){
        this.pageContent = pageContent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getAbstract() {
        return Abstract;
    }

    public void setAbstract(String Abstract){
        this.Abstract = Abstract;
    }

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName){
        this.webName = webName;
    }

    public WordVector getMyVec() {
        return myVec;
    }

    public void setMyVec(WordVector myVec){
        this.myVec = myVec;
    }


    public void addWordVec(WordVector vec, String spaceID) {
        if(vec == null )
            return ;
        if(myVec == null) {
            myVec = vec ;
            myVec.setParentInfo(this);
            return ;
        }
        for(int i = 0 ; i < vec.getVectors().size(); i ++) {
            wordCount wc =  vec.getVectors().get(i) ;
            Boolean flag = false ;
            for(int j= 0 ; j < myVec.getVectors().size(); j ++) {
                wordCount wc2 =  myVec.getVectors().get(j) ;
                if(wc.getWord().equals(wc2.getWord())) {
                    flag = true ;
                    wc2.setCount(wc2.getCount() + wc.getCount());
                    break;
                }
            }
            if(flag)
                continue ;
            else
                myVec.getVectors().add(wc);
        }
    }
}

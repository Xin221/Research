package inetintelliprocess.searchengine.searchers.otherQuery;

import inetintelliprocess.bean.KeyWord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class abasicQuery {

    public String getHTML(String basicUrl, List<KeyWord> keyList) throws IOException
    {
        String key = URLEncoder.encode(keyList.get(0).getWord(), "utf-8");
        KeyWord str = null;
        for(int i=1;i<keyList.size();i++){
            str = keyList.get(i);
            key = key +"+"+URLEncoder.encode(str.getWord(), "utf-8");
        }
        key = key + "+"+ URLEncoder.encode("地震","utf-8");
        StringBuilder sb=new StringBuilder();
        String path=basicUrl+key;
        System.out.println("path is "+path);
        URL url=new URL(path);
        BufferedReader breader=new BufferedReader(new InputStreamReader(url.openStream(),"utf-8"));
        String line=null;
        while((line=breader.readLine())!=null)
        {
            sb.append(line);
        }
        return sb.toString();
    }
}

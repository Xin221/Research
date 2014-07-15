package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.util.LogWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.MDC;

public class eDeathInfo {

    public String extrDeathInfo(String pageInfo)
    {
        String deathInfo = new String();
        if(pageInfo.contains("kills") || pageInfo.contains("killed") || pageInfo.contains("killing"))
        {
            //Pattern patPrefix = Pattern.compile("(more than)?((\\d+.\\d+)|(\\d))++(a-z)*(A-Z)*+(were)?killed");
            Pattern patPrefix = Pattern.compile("((more than )|(More than ))?((at least )|(At least))?((((\\d+.\\d+)|(\\d) | (\\d+,\\d+))++)|( dozens))( people)?(( were killed)|(dead))");
            Pattern patSuffix = Pattern.compile("((killing )|(kills )|(killed ))(more than )?(at least )?((((\\d+.\\d+)|(\\d)|(\\d+,\\d+))++)|(dozens))( people)?");
            Matcher matPrefix = patPrefix.matcher(pageInfo);
            Matcher matSuffix = patSuffix.matcher(pageInfo);
            String deathPrefix = null;
            String deathSuffix = null;
            while(matPrefix.find())
            {
                deathPrefix = matPrefix.group();
            }
            while(matSuffix.find())
            {
                deathSuffix = matSuffix.group();
            }
            if(deathPrefix != null)
            {
                System.out.println("-------------------------------------------" + String.valueOf(deathPrefix));
                deathInfo = deathPrefix;
            }
            if(deathSuffix != null)
            {
                System.out.println("-------------------------------------------" + String.valueOf(deathSuffix));
                deathInfo = deathSuffix;
            }
        }

        return deathInfo;
    }
    public String extrInjureInfo(String pageInfo)
    {
        String injureInfo = new String();
        if(pageInfo.contains("injured") || pageInfo.contains("injuring") || pageInfo.contains("injures"))
        {
            Pattern patPrefix = Pattern.compile("((more than )|(More than ))?((at least )|(At least ))?((((\\d+.\\d+)|(\\d)|(\\d+,\\d+))++)|(dozens))( people)?(( were)? injured)");
            Pattern patSuffix = Pattern.compile("((injuring )|(leaving )|(Injuring ) | (Leaving ))(more than )?(at least )?((((\\d+.\\d+)|(\\d))++)|(dozens))( people)?( others)?");
            Matcher matPrefix = patPrefix.matcher(pageInfo);
            Matcher matSuffix = patSuffix.matcher(pageInfo);
            String injurePrefix = null;
            String injureSuffix = null;
            while(matPrefix.find())
            {
                injurePrefix = matPrefix.group();
            }
            while(matSuffix.find())
            {
                injureSuffix = matSuffix.group();
            }
            if(injurePrefix != null)
            {
                System.out.println("-------------------------------------------" + String.valueOf(injurePrefix));
                injureInfo = injurePrefix;
            }
            if(injureSuffix != null)
            {
                System.out.println("-------------------------------------------" + String.valueOf(injureSuffix));
                injureInfo = injureSuffix;
            }

        }
        return injureInfo;
    }
    public String extrBuildingInfo(String pageInfo)
    {
        String buildingInfo = new String();
        if(pageInfo.contains("buildings") || pageInfo.contains("rooms") || pageInfo.contains("homes") || pageInfo.contains("building")|pageInfo.contains("houses"))
        {
            Pattern patPrefix = Pattern.compile("((destroying |damaging |destroyed |damaged )((thousands of )|(hundreds of ))?((more than )|(at least )|(over ))?((\\d+.\\d+)|(\\d)|(\\d+,\\d+))??( )*(million )?((buildings )|(rooms )|(homes )|(houses )))|((thousands of )?((more than )|(More than )|(at least )|(At least)|(over ))?((\\d+.\\d+)|(\\d)|(\\d+,\\d+))??( )*(million )?((buildings )|(rooms )|(homes )|(houses ))(were )?(destroyed|damaged))|((thousands of )?((more than )|(More than )|(at least )|(At least)|(over ))?((\\d+.\\d+)|(\\d)|(\\d+,\\d+))??( )*((damaged)|(destroyed))((buildings)|(rooms)|(homes)|(houses)))");
            //Pattern patPrefix = Pattern.compile("(over )((\\d+.\\d+)|(\\d)|(\\d+,\\d+))++( )*(million )?(rooms )(were )(damaged)");
            Matcher matPrefix = patPrefix.matcher(pageInfo);
            String consPrefix = null;
            while(matPrefix.find())
            {
                consPrefix = matPrefix.group();
            }
            if(consPrefix != null)
            {
                System.out.println("-------------------------------------------" + String.valueOf(consPrefix));
                buildingInfo = consPrefix;
            }

        }

        return buildingInfo;
    }
    public void writeToDB(String deathInfo, String injureInfo,String buildingInfo, String url, String webName, String eventID){
        //把死亡信息作为一个记录插入eventinfo表中
        WordManageDBO loader = new WordManageDBO() ;
        try {
            if (!loader.updateDeathInfo(deathInfo, injureInfo,buildingInfo,  url, webName, eventID))
                System.out.println("更新英文死亡、受伤人员信息不成功");
        }
        catch(Exception exp) {
            MDC.put("eventID", eventID);
            LogWriter.logger.warn("分析器:更新事件" + eventID + "灾情报告异常");
        }
    }

    public static void main(String args[]) {
        //String pageInfo = "|||||||日本预测南部海域9级地震将有32万人1万个建筑物倾塌个丧生共条视频2012年08月31日14:10来源：分享到...转发：视频地址：通过MSN、QQ告诉你的好友flash地址：复制链接到博客或论坛html代码：复制播放器到博客或论坛热点娱乐养生相关新闻视频介绍进入《》专题　　围绕日本中西部沿海的“南海海槽”将发生大地震的可能性，日本29日举行的地震专家委员会公布了预测结果。委员会公布，如大地震发生，地震强度将是东日本大地震的1.8倍，引发的海啸会直接袭击日本东部海岸沿线大部分城市；死亡人数预计最多将达32万3千人，这比日本2003年预测的结果高出13倍；且海啸将淹没滨冈核电站(静冈县御前崎市)。委员会表示中央政府及各地方政府必须从根本上重新制定地震防御措施。电视，综合报道。向您推荐如下视频（责任编辑：董子龙、张舒)视频推荐版权所有，未经书面授权禁止使用Copyright&copy;1997-2012bywww.people.com.cn.allrightsreserved";
        //String pageInfo = "The quakes hit Gansu province on Monday morning, with the majority of casualties in Dingxi city. at least 800 people were killed.";
        String pageInfo = "The earthquake struck the steep hills of Lushan county at 08:02 local time (00:02 GMT), triggering landslides and leaving thousands of homes destroyed.";
        eDeathInfo death = new eDeathInfo();
        System.out.println(death.extrDeathInfo(pageInfo));
        System.out.println(death.extrInjureInfo(pageInfo));
        System.out.println(death.extrBuildingInfo(pageInfo));
    }
}

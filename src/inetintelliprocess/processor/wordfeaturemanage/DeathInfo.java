package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.util.LogWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.MDC;

public class DeathInfo {
    /**
     * 提取地震死亡情况信息
     * @author WQH
     * @param pageInfo 文本信息
     */
    public String extrDeathInfo(String pageInfo) {
        String deathInfo = null;
        if(pageInfo.contains("死") || pageInfo.contains("遇难") || pageInfo.contains("亡")){
            Pattern patPrefix = Pattern.compile("((\\d+.\\d+)|(\\d))++([\\u4e00-\\u9fa5])*(:)?(万人|人)((死亡)|(死)|(遇难)|(丧生))");
            Pattern patSuffix = Pattern.compile("((死亡)|(死)|(遇难)|(丧生))([\\u4e00-\\u9fa5])*(:)?((\\d+.\\d+)|(\\d))++(万人|人)");
            Pattern patNoDie = Pattern.compile("((暂无)|(尚无)|(无)|(未))([\\u4e00-\\u9fa5])*(:)?((伤亡)|(受伤)|(死亡)|(遇难))");
            Matcher matPrefix = patPrefix.matcher(pageInfo);
            Matcher matSuffix = patSuffix.matcher(pageInfo);
            Matcher matNoDie = patNoDie.matcher(pageInfo);
            String deathPrefix = null;
            String deathSuffix = null;
            String deathNo = null;
            while(matPrefix.find()){
                deathPrefix = matPrefix.group();
            }
            while(matSuffix.find()){
                deathSuffix = matSuffix.group();
            }
            while(matNoDie.find()){
                deathNo = matNoDie.group();
            }
            if (deathPrefix != null){
                System.out.println("-------------------------------------------" + String.valueOf(deathPrefix));
                deathInfo = deathPrefix;
            }
            else if (deathSuffix != null){
                System.out.println("-------------------------------------------" + String.valueOf(deathSuffix));
                deathInfo = deathSuffix;
            }
            else if (deathNo != null){
                System.out.println("-------------------------------------------" + String.valueOf(deathNo));
                deathInfo = deathNo;
            }
        }
        return deathInfo;
    }

    /**
     * 提取地震受伤、受灾情况信息
     * @author WQH
     * @param pageInfo 文本信息
     */
    public String extrInjureInfo(String pageInfo) {
        String injureInfo = null;
        if(pageInfo.contains("伤") || pageInfo.contains("受灾")){
            Pattern patPrefix = Pattern.compile("((\\d+.\\d+)|(\\d))++([\\u4e00-\\u9fa5])*(:)?(万人|人)((受伤)|(受灾)|(伤))");
            Matcher matPrefix = patPrefix.matcher(pageInfo);
            String deathPrefix = null;
            String deathSuffix = null;
            String deathNo = null;
            while(matPrefix.find()){
                deathPrefix = matPrefix.group();
            }
            if (deathPrefix != null){
                System.out.println("-------------------------------------------" + String.valueOf(deathPrefix));
                injureInfo = deathPrefix;
            } else {
                Pattern patSuffix = Pattern.compile("((受伤)|(伤)|(受灾))([\\u4e00-\\u9fa5])*(:)?((\\d+.\\d+)|(\\d))++(万人|人)");
                Matcher matSuffix = patSuffix.matcher(pageInfo);
                while(matSuffix.find()){
                    deathSuffix = matSuffix.group();
                }
                if (deathSuffix != null){
                    System.out.println("-------------------------------------------" + String.valueOf(deathSuffix));
                    injureInfo = deathSuffix;
                }else {
                    Pattern patNoDie = Pattern.compile("((暂无)|(尚无)|(无)|(未))([\\u4e00-\\u9fa5])*(:)?((伤亡)|(受伤)|(死亡)|(遇难))");
                    Matcher matNoDie = patNoDie.matcher(pageInfo);
                    while(matNoDie.find()){
                        deathNo = matNoDie.group();
                    }
                    if (deathNo != null) {
                        System.out.println("-------------------------------------------" + String.valueOf(deathNo));
                        injureInfo = deathNo;
                    }
                }
            }
        }
        return injureInfo;
    }

    /**
     * 提取地震建筑物损毁情况信息
     * @param pageInfo 文本信息
     */
    public String extrBuildingInfo(String pageInfo) {
        String buildingInfo = null;
        if(pageInfo.contains("建筑物") || pageInfo.contains("房屋")){
            Pattern patPrefix = Pattern.compile("((多处)|(((\\d+.\\d+)|(\\d))++(万个|个)))?(建筑物|房屋|房子|建筑)((几乎)|(全部))*([\\u4e00-\\u9fa5])*(:)?((.塌)|(.坏)|(受损)|(龟裂))");
            Matcher matPrefix = patPrefix.matcher(pageInfo);
            String consPrefix = null;
            String consSuffix = null;
            String consNo = null;
            while(matPrefix.find()){
                consPrefix = matPrefix.group();
            }
            if (consPrefix != null){
                System.out.println("-------------------------------------------" + String.valueOf(consPrefix));
                buildingInfo = consPrefix;
            } //else {
            Pattern patSuffix = Pattern.compile("((倒塌)|(倾塌)|(毁坏)|(受损))([\\u4e00-\\u9fa5])*(:)?((\\d+.\\d+)|(\\d))++(万个|个)((倒塌)|(倾塌)|(毁坏)|(受损)|(损坏)|(龟裂))");
            Matcher matSuffix = patSuffix.matcher(pageInfo);
            while(matSuffix.find()){
                consSuffix = matSuffix.group();
            }
            if (consSuffix != null){
                System.out.println("-------------------------------------------" + String.valueOf(consSuffix));
                buildingInfo = consSuffix;
            }//else {
            Pattern patNoDie = Pattern.compile("((暂无)|(尚无)|(无)|(未)|(没有))([\\u4e00-\\u9fa5])*(:)?((倒塌)|(倾塌)|(毁坏)|(受损)|(损坏)|(龟裂))");
            Matcher matNoDie = patNoDie.matcher(pageInfo);
            while(matNoDie.find()){
                consNo = matNoDie.group();
            }
            if (consNo != null) {
                System.out.println("-------------------------------------------" + String.valueOf(consNo));
                buildingInfo = consNo;
            }
            //}
            //}
        }
        return buildingInfo;
    }

    /**
     * 更新数据表中的死、伤亡信息
     * @author WQH
     * @param deathInfo 死亡信息, injureInfo 受伤、受灾信息, eventID 警告事件标识
     */
    public void writeToDB(String deathInfo, String injureInfo,String buildingInfo, String url, String webName, String eventID){
        //把死亡信息作为一个记录插入eventinfo表中
        WordManageDBO loader = new WordManageDBO() ;
        try {
            if (!loader.updateDeathInfo(deathInfo, injureInfo,buildingInfo,  url, webName, eventID))
                System.out.println("更新死亡、受伤人员信息不成功");
        }
        catch(Exception exp) {
            MDC.put("eventID", eventID);
            LogWriter.logger.warn("分析器:更新事件" + eventID + "灾情报告异常");
        }
    }
    /**
     * 提取文本信息中死亡人数信息，以及受伤、受灾人数信息的测试
     * @author WQH
     */
    public static void main(String args[]) {
        //String pageInfo = "|||||||日本预测南部海域9级地震将有32万人1万个建筑物倾塌个丧生共条视频2012年08月31日14:10来源：分享到...转发：视频地址：通过MSN、QQ告诉你的好友flash地址：复制链接到博客或论坛html代码：复制播放器到博客或论坛热点娱乐养生相关新闻视频介绍进入《》专题　　围绕日本中西部沿海的“南海海槽”将发生大地震的可能性，日本29日举行的地震专家委员会公布了预测结果。委员会公布，如大地震发生，地震强度将是东日本大地震的1.8倍，引发的海啸会直接袭击日本东部海岸沿线大部分城市；死亡人数预计最多将达32万3千人，这比日本2003年预测的结果高出13倍；且海啸将淹没滨冈核电站(静冈县御前崎市)。委员会表示中央政府及各地方政府必须从根本上重新制定地震防御措施。电视，综合报道。向您推荐如下视频（责任编辑：董子龙、张舒)视频推荐版权所有，未经书面授权禁止使用Copyright&copy;1997-2012bywww.people.com.cn.allrightsreserved";
        String pageInfo = "“4·20”四川雅安芦山7级地震造成巨大损失，据雅安市宝兴县县长介绍，目前，全县水电气中断，房屋几乎全部受损，包括汶川地震后重建的建筑。未发现房屋严重倒塌和受困人员。";
        DeathInfo death = new DeathInfo();
        System.out.println(death.extrDeathInfo(pageInfo));
        System.out.println(death.extrInjureInfo(pageInfo));
        System.out.println(death.extrBuildingInfo(pageInfo));
    }
}

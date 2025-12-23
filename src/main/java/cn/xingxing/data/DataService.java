package cn.xingxing.data;


/**
 * @Author: yangyuanliang
 * @Date: 2025-12-22
 * @Version: 1.0
 */
public interface DataService {
   /**
    * 获取当日比赛信息
    *
    */
   int loadMatchInfoData();
   /**
    * 获取当日比赛交锋记录
    *
    */
   int loadHistoryData(Integer matchId);

   /**
    * 获取当日比赛赔率变化信息
    */
   int loadHadListData(Integer matchId);



   /**
    * 获取当日比赛赔率变化信息
    */
   int loadSimilarMatch(Integer matchId);


   int loadMatchResult(Integer matchId);


   int afterMatchAnalysis(Integer matchId);

}

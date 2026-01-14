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
   int syncMatchInfoData();
   /**
    * 获取当日比赛交锋记录
    *
    */
   int syncHistoryData();

   int loadHistoryDataByMatchId(String matchId);

   /**
    * 获取当日比赛赔率变化信息
    */
   int syncHadListData();

   /**
    * 获取当日比赛赔率变化信息
    */

   int syncSimilarMatch();

   int loadSimilarMatchByMatchId(String matchId);


   int syncMatchResult();

   /**
    * 赛后复盘
    * @param
    * @return
    */
   int afterMatchAnalysis();

   /**
    * 获取xg数据
    * @return
    */
   int loadTeamStats();

   int loadTeamStatsHome();

   int loadTeamStatsAway();

   void syncHadListByMatchId(String matchId);
}

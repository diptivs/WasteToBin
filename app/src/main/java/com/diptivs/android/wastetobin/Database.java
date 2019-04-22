package com.diptivs.android.wastetobin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Database {


  private static Database instance = new Database();

  private Map<String, Constants.Categories> database = new HashMap<>();
  private List<String> totalList = new ArrayList<>();

  private Database(){}

  public static Database getInstance() {
    return instance;
  }

  public void retrieveInformationFromFireBaseObj(List<String> binItems, Constants.Categories categoryFileId)
  {
      for(int i=0;i<binItems.size();i++)
      {
        String line = binItems.get(i);
        this.database.put(line, categoryFileId);
        this.totalList.add(line);
      }
  }
  public void retrieveInformationFromCategoryFile(InputStream stream, Constants.Categories categoryFileId) throws IOException {
      BufferedReader br = new BufferedReader(new InputStreamReader(stream));
      while (br.ready()) {
        String line = br.readLine();
        //Log.d(Constants.TAGDipti,line);
        this.database.put(line, categoryFileId);
        this.totalList.add(line);
      }
  }

  public void sortTotalList() {
    Collections.sort(this.totalList);
  }

  public int diff(String strOne, String strTwo, int indexOne, int indexTwo) {
    if (strOne.charAt(indexOne) == strTwo.charAt(indexTwo)) {
      return 0;
    } else {
      return 1;
    }
  }

  public int editDistance(String strOne, String strTwo) {
    int strOneLen = strOne.length();
    int strTwoLen = strTwo.length();
    int inf = 1000000;
    int[][] dp = new int[strOneLen][strTwoLen];

    for (int i = 0; i < strOneLen; i++) {
      for (int j = 0; j < strTwoLen; j++) {
        dp[i][j] = inf;
      }
    }

    for (int i = 0; i < strOneLen; i++) {
      for (int j = 0; j < strTwoLen; j++) {
        if (i == 0) {
          dp[i][j] = j;
        } else if (j == 0) {
          dp[i][j] = i;
        } else {
          dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i][j]);
          dp[i][j] = Math.min(dp[i - 1][j - 1] + diff(strOne, strTwo, i - 1, j - 1), dp[i][j]);
        }
      }
    }
    return dp[strOneLen - 1][strTwoLen - 1];
  }



  public String initialQuery(String query) {
    //Log.d(Constants.TAGDipti,"initialQuery: "+query);
    Constants.Categories category = this.database.get(query);

    return (category != null) ? Constants.categoriesName[category.ordinal()] : null;
  }


  public List<String> secondaryQuery(String query) {
    List<String> list = new ArrayList<String>();
    List<Pair<Integer, String>> list2 = new ArrayList<Pair<Integer, String>>();
    List<String> suggestions = new ArrayList<String>();

    for (String it : this.totalList) {
      if (it.contains(query)) {
        list.add(it);
      }
    }
    Collections.sort(list);
    for (int i = 0; i < Constants.LIMIT && i < list.size(); i++) {
      list2.add(new Pair<Integer, String>(editDistance(list.get(i), query), list.get(i)));
    }
    Collections.sort(list2);
    for (int i = 0; i < Constants.LIMIT && i < list2.size(); i++) {
      suggestions.add(list2.get(i).second);
    }
    return suggestions.size() == 0 ? null : suggestions;
  }


  public List<String> getTotalList() {
    return this.totalList;
  }
}

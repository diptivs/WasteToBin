package com.diptivs.android.wastetobin;

import android.util.Log;

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
    // arbitrarily set the cost for each to be infinity
    for (int i = 0; i < strOneLen; i++) {
      for (int j = 0; j < strTwoLen; j++) {
        dp[i][j] = inf;
      }
    }
    // Now we calculate the cost or "edit distance" associated with the
    // sub strings of String A and String B. We then build off this by
    // doing some dynamic programming logic
    for (int i = 0; i < strOneLen; i++) {
      for (int j = 0; j < strTwoLen; j++) {
        if (i == 0) { 
          // In this case it takes j characters two form the first i characters of strOne
          dp[i][j] = j;
        } else if (j == 0) {
          // In this case it takes j characters two form the first i characters of strTwo 
          dp[i][j] = i;
        } else {
          // in this case it takes minimum of of the dp state itself, and
          // creating a new string from previous states
          dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i][j]);
          dp[i][j] = Math.min(dp[i - 1][j - 1] + diff(strOne, strTwo, i - 1, j - 1), dp[i][j]);
        }
      }
    }
    return dp[strOneLen - 1][strTwoLen - 1];
  }



  public String initialQuery(String query) {
    Log.d(Constants.TAGDipti,"initialQuery: "+query);
    Constants.Categories category = this.database.get(query);

    return (category != null) ? Constants.categoriesName[category.ordinal()] : null;
  }


  public List<String> secondaryQuery(String query) {
    List<String> list = new ArrayList<String>();
    List<Pair<Integer, String>> list2 = new ArrayList<Pair<Integer, String>>();
    List<String> suggestions = new ArrayList<String>();
    // checks if the query if a substring
    for (String it : this.totalList) {
      if (it.contains(query)) {
        list.add(it);
      }
    }
    Collections.sort(list);
    // Now we sort will get a baring of how close these strings are by
    // using the edit distance between the two strings.
    for (int i = 0; i < Constants.LIMIT && i < list.size(); i++) {
      list2.add(new Pair<Integer, String>(editDistance(list.get(i), query), list.get(i)));
    }
    Collections.sort(list2);
    // Now we'll add the first this.LIMIT words that are most similar to the
    // user's query
    for (int i = 0; i < Constants.LIMIT && i < list2.size(); i++) {
      suggestions.add(list2.get(i).second);
    }
    return suggestions.size() == 0 ? null : suggestions;
  }

  /** Returns the a list containing all of the terms in the database. */
  public List<String> getTotalList() {
    return this.totalList;
  }
}

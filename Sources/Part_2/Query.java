package Part_2;

import java.util.HashMap;

/**
 * This class represents a query that needs to be parsed
 */
public class Query {

    private HashMap<String, int[]> queryTermDic;
    private String text;

    public Query(String queryText) {
        this.text = queryText;
    }


    public String getQueryText() {
        return text;
    }


    public void setQueryTermDic(HashMap<String, int[]> queryTermDic) {
        this.queryTermDic = queryTermDic;
    }


    public HashMap<String, int[]> getQueryTermDic() {
        return queryTermDic;
    }

}

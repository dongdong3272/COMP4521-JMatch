package edu.hkust.jmatch.backend;

import android.content.Context;

public class Global {

    public static String accessToken = "";
    public static String env = "jmatch-2g4kgt6q3ac45476";
    public static Context context = null;

    public static class User {
        public static Boolean loggedIn = false;
        public static String username = "";
        public static boolean isCandidate = true;
    }

    public static class Document {
        public static String documentID = "";
    }

    public static class BaseURL {
        // REQUEST METHOD: POST
        // REQUEST URL: https://api.weixin.qq.com/tcb/databasequery?
        // PARAMETERS: access_token=ACCESS_TOKEN
        // access_token : 接口调用凭证
        // env : 云开发环境ID
        // query : 数据库操作语句
        public static String queryBaseURL = "https://api.weixin.qq.com/tcb/databasequery?";

        // REQUEST METHOD: POST
        // REQUEST URL: https://api.weixin.qq.com/tcb/databaseadd?
        // PARAMETERS: access_token=ACCESS_TOKEN
        // access_token : 接口调用凭证
        // env : 云开发环境ID
        // query : 数据库操作语句
        public static String addBaseURL = "https://api.weixin.qq.com/tcb/databaseadd?";

        // REQUEST METHOD: POST
        // REQUEST URL: https://api.weixin.qq.com/tcb/invokecloudfunction?
        // PARAMETERS: access_token=ACCESS_TOKEN&env=ENV&name=FUNCTION_NAME
        // access_token : 接口调用凭证
        // env : 云开发环境ID
        // name : 云函数名称
        // POSTBODY : 云函数的传入参数，具体结构由开发者定义。
        public static String cloudFunctionURL = "https://api.weixin.qq.com/tcb/invokecloudfunction?";
    }

    public static void retrieveAccessToken() {
        new AccessToken().execute();
    }

}

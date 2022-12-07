package com.densoft.springbootbasicauth.util;

import javax.servlet.http.HttpServletRequest;

public class Utility {

    public static String getStaticSiteUrl(HttpServletRequest request){
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(),"");
    }
}

package com.shepherd.redbookuserservice.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/7/14 19:23
 */
public class CookBaseSessionUtils {

    //cookie名称
    private String cookieName = "red-book-permission-shepherd";

    public String getRequestedSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie == null) {
                continue;
            }
            if (! cookieName.equalsIgnoreCase(cookie.getName())) {
                continue;
            }

            return cookie.getValue();
        }
        return null;
    }

    public void onNewSession(HttpServletRequest request,
                             HttpServletResponse response) {
        String sessionId = (String) request.getAttribute(cookieName);
        Cookie cookie = new Cookie(cookieName, sessionId);
//        cookie.setDomain(request.getRemoteHost());
        cookie.setHttpOnly(true);
        cookie.setPath(request.getContextPath() + "/");
        cookie.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(cookie);
    }

}

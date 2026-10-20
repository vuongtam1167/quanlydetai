package com.example.quanlydetai.dto;

/** Một mục menu ở sidebar. ready=false -> hiển thị mờ, chưa có link. */
public record MenuItem(String label, String icon, String url, boolean ready) {

    public boolean isActive(String currentPath) {
        if (currentPath == null || url == null) {
            return false;
        }
        if ("/".equals(url)) {
            return "/".equals(currentPath) || "/home".equals(currentPath);
        }
        return currentPath.equals(url) || currentPath.startsWith(url + "/");
    }
}

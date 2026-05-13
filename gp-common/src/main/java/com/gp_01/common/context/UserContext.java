package com.gp_01.common.context;

public class UserContext {

    private static final ThreadLocal<Long> TL = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 保存用户id
     *
     * @param id
     */
    public static void setUser(Long id) {
        TL.set(id);
    }

    /**
     * 获取用户id
     *
     * @return
     */
    public static Long getUser() {
        return TL.get();
    }

    /**
     * 删除用户id
     */
    public static void removeUser() {
        TL.remove();
    }


}

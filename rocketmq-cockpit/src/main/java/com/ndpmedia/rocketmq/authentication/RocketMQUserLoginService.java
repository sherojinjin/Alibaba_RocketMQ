package com.ndpmedia.rocketmq.authentication;

/**
 * the interface of rocekmq user service.
 * log the user login status : 0 success, 1 failed.
 * find the login users ;
 * find the user status ;
 * lock the user ;
 * unlock the user ;
 */
public interface RocketMQUserLoginService
{
    /**
     *  (user status 1 & lock time > now) ,user locked;
     *  another ,user locked;
     * @param username  which user we want
     * @return  the user status
     */
    boolean findUserStatus(String username);

    boolean logUserStatus(String username);

    boolean lockUser(String username);

    boolean unlockUser(String username);

    boolean userRetryTimeAdd(String username);
}

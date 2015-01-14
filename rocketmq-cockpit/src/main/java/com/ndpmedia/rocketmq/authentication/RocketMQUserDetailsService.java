package com.ndpmedia.rocketmq.authentication;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.cockpit.log.CockpitLogger;
import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * authentication.
 */
public class RocketMQUserDetailsService implements UserDetailsService
{
    private CockpitDao cockpitDao;

    private final Logger logger = CockpitLogger.getLogger();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        UserDetails user = null;
        try
        {
            System.out.println(" try to login ====username===== " + username);
            Map<String, Object> map = getUser(username);

            if (map == null )
            {
                throw new Exception(" this user need register first :" + username);
            }

            logger.debug(" try to login ====getUser====== " + map.get("username") + " " + map.get("password"));

            // 用户名、密码、是否启用、是否被锁定、是否过期、权限
            user = new User(username, map.get("password").toString(), true, true, true, true,
                    getAuthority("ROLE_ADMIN"));
            
        }
        catch (Exception e)
        {
            logger.error(" log faied !" + e.getMessage());
            throw new UsernameNotFoundException(" log faied !" + e.getMessage());
        }

        return user;
    }

    /**
     * try to get the role of the user
     *
     * @param role user role type,just admin for every  one.
     * @return the role of user
     */
    public Collection<GrantedAuthority> getAuthority(String role)
    {
        List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

        authList.add(new SimpleGrantedAuthority(role));

        return authList;
    }

    private Map<String, Object> getUser(String username) throws Exception
    {
        String sql = " select * from cockpit_user where username='" + username + "'";

        return cockpitDao.getFirstRow(sql);
    }

    public CockpitDao getCockpitDao()
    {
        return cockpitDao;
    }

    public void setCockpitDao(CockpitDao cockpitDao)
    {
        this.cockpitDao = cockpitDao;
    }


}

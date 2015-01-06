package com.ndpmedia.rocketmq.authentication;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = null;
        try
        {
            System.out.println(" try to login ====username===== " + username);
            System.out.println(" try to login ====password===== " + new Md5PasswordEncoder().encodePassword("password", "root"));
            Map<String, Object> map = getUser(username);
            // 用户名、密码、是否启用、是否被锁定、是否过期、权限
            user = new User("root", new Md5PasswordEncoder().encodePassword("password", "root"),
                    true, true, true, true, getAuthority(""));
            
        }
        catch (Exception e)
        {
            throw  new UsernameNotFoundException("");
        }

        return user;
    }

    /**
     * try to get the role of the user
     * @param role
     * @return
     */
    public Collection<GrantedAuthority> getAuthority(String role)
    {
        List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

        authList.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        return authList;
    }

    private Map<String, Object> getUser(String username) throws Exception
    {
        String sql = " select * from cockpit_user where username='" + username + "'";
        List<Map<String, Object>> list = cockpitDao.getList(sql);
        if (list.isEmpty() || list.size() > 1)
            throw new Exception("");
        return list.get(0);
    }

    public CockpitDao getCockpitDao() {
        return cockpitDao;
    }

    public void setCockpitDao(CockpitDao cockpitDao) {
        this.cockpitDao = cockpitDao;
    }

}

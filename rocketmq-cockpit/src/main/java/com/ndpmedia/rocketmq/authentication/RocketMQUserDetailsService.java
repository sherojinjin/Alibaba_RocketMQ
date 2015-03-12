package com.ndpmedia.rocketmq.authentication;

import com.ndpmedia.rocketmq.cockpit.model.CockpitUser;
import com.ndpmedia.rocketmq.cockpit.mybatis.mapper.CockpitUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * authentication.
 */
public class RocketMQUserDetailsService implements UserDetailsService {

    @Autowired
    private CockpitUserMapper cockpitUserMapper;

    private final Logger logger = LoggerFactory.getLogger(RocketMQUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserDetails user = null;

        try {
            logger.debug("[login] try to login ====userName===== " + userName);
            CockpitUser cockpitUser = getCockpitUser(userName);

            if (cockpitUser == null) {
                throw new Exception("Credentials incorrect.");
            }

            logger.debug("[login] try to login as " + cockpitUser.getUsername());

            // 用户名、密码、是否启用、是否被锁定、是否过期、权限
            user = new User(userName, cockpitUser.getPassword(), true, true, true, true,
                    getAuthority(cockpitUser.getRole()));
        } catch (Exception e) {
            logger.warn(" log failed !" + e.getMessage());
            throw new UsernameNotFoundException(" log failed !" + e.getMessage());
        }

        return user;
    }

    /**
     * try to get the role of the user
     *
     * @param role user role type,just admin for every  one.
     * @return the role of user
     */
    public Collection<GrantedAuthority> getAuthority(String role) {
        List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

        authList.add(new SimpleGrantedAuthority(role));

        return authList;
    }

    private CockpitUser getCockpitUser(String userName) throws Exception {
        return cockpitUserMapper.getByUserName(userName);
    }
}

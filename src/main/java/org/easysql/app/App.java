package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.bean.User;
import org.easysql.helper.CommonValue;
import org.easysql.info.FillData;
import org.easysql.plugin.pageHelper.CachePageHelper;
import org.easysql.session.Cache;
import org.easysql.session.SessionHandler;
import org.easysql.session.SqlSession;

import java.util.ArrayList;


public class App extends EasySqlApplication {
    public static void main(String[] args) {
        init(App.class);
        SessionHandler<User> handler = handler(User.class);
        User user= handler.selectAsID(1);
        DataOutput(user);
        handler.deleteAsID(user);
    }
}

package com.xiaoyi.redis.support;

/**
 * Created on 2021/1/6.
 *
 * @author 小逸
 * @description
 */
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.util.List;

public interface CacheNames {
    String CHOICE_LABEL = "CHOICE_LABEL";
    String CHOICE_VALUE = "CHOICE_VALUE";
    String ALL_DB_CHOICES = "ALL_DB_CHOICE";
    String USER_PERMISSION_CUSTOM_SSO = "USER_PERMISSION_CUSTOM_SSO";
    String USER_MENU_PERMISSION_CUSTOM_SSO = "USER_MENU_PERMISSION_CUSTOM_SSO";
    String PERMISSION = "PERMISSION";
    String USER_INFO = "USER_INFO";
    String ROLE = "ROLE";
    String ALL_ROLE_MENU_PERMISSION = "ALL_ROLE_MENU_PERMISSION";
    String ALL_USER_MENU_PERMISSION = "ALL_USER_MENU_PERMISSION";
    String ROLE_VPD = "ROLE_VPD";
    String ROLE_FIELD_PERMISSION = "ROLE_FIELD_PERMISSION";
    String ROLE_DIMENSION_CONTEXT = "ROLE_DIMENSION_CONTEXT";
    String ROLE_GRANT = "ROLE_GRANT";
    String EVENT = "EVENT";
    String EVENT_HANDLER = "EVENT_HANDLER";
    String SEARCH_TEMPLATE = "SEARCH_TEMPLATE";
    String PREFERENCE_BOOLEAN = "PREFERENCE_BOOLEAN";
    String PREFERENCE_INTEGER = "PREFERENCE_INTEGER";
    String PREFERENCE_STRING = "PREFERENCE_STRING";
    String PREFERENCE_ZONEID = "PREFERENCE_ZONEID";
    String PREFERENCES = "PREFERENCES";
    String PREFERENCE_KEY_PREFIX = "PREFERENCE_KEY_PREFIX";
    String BUSINESS_NUMBER = "BUSINESS_NUMBER";
    String NOTIFICATIONS = "NOTIFICATIONS";
    String NOTIFICATION_READERS = "NOTIFICATION_READERS";
    String USERS_UNREAD_NOTIFICATIONS = "USERS_UNREAD_NOTIFICATIONS";
    String WEIXIN = "WEIXIN";
    String MESSAGE_SOURCE = "MESSAGE_SOURCE";
    String TASK_SERVER_ALIVE_REPORT = "TASK_SERVER_ALIVE_REPORT";
    String SSO_SERVICE_TICKET = "SSO_SERVICE_TICKET";
    String HANDLER_CLIENT_SECRET = "HANDLER_CLIENT_SECRET";
    String HANDLER_CLIENT_PERMISSION = "HANDLER_CLIENT_PERMISSION";
    String HANDLER_CLIENT_NONCE = "HANDLER_CLIENT_NONCE";
    String TOTP_SECRET_KEY = "TOTP_SECRET_KEY";
    String QQ_BOUND_MESSAGE_SUCCESS = "QQ_BOUND_MESSAGE_SUCCESS";
    String QQ_BOUND_MESSAGE_FAILED = "QQ_BOUND_MESSAGE_FAILED";
    String WX_BOUND_MESSAGE_SUCCESS = "WX_BOUND_MESSAGE_SUCCESS";
    String WX_BOUND_MESSAGE_FAILED = "WX_BOUND_MESSAGE_FAILED";
    String DT_BOUND_MESSAGE_SUCCESS = "DT_BOUND_MESSAGE_SUCCESS";
    String DT_BOUND_MESSAGE_FAILED = "DT_BOUND_MESSAGE_FAILED";
    String FAVORITE_MENU = "FAVORITE_MENU";

    static List<String> names() {
        List<String> nameList = Lists.newArrayList();
        Class clazz = CacheNames.class;
        Field[] var2 = clazz.getDeclaredFields();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Field f = var2[var4];

            try {
                nameList.add(f.get((Object)null).toString());
            } catch (IllegalAccessException var7) {
                var7.printStackTrace();
            }
        }

        return nameList;
    }
}

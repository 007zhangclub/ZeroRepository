package com.bjpowernode.crm.settings.base;

import com.bjpowernode.crm.base.Base;
import com.bjpowernode.crm.settings.service.DictionaryService;
import com.bjpowernode.crm.settings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class Settings extends Base {

    @Autowired
    protected DictionaryService dictionaryService;

    @Autowired
    protected UserService userService;
}

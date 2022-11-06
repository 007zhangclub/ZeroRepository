package com.bjpowernode.crm.settings.service.impl;

import com.bjpowernode.crm.settings.dao.DictionaryTypeDao;
import com.bjpowernode.crm.settings.domain.DictionaryType;
import com.bjpowernode.crm.settings.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
    字典模块业务层对象
        字典类型的dao对象
        字典值的dao对象
 */
@Service
public class DictionaryServiceImpl implements DictionaryService {

    @Autowired
    private DictionaryTypeDao dictionaryTypeDao;

    @Override
    public List<DictionaryType> findDictionaryTypeList() {
        return dictionaryTypeDao.findAll();
    }

    @Override
    public DictionaryType findDictionaryType(String code) {
        return dictionaryTypeDao.findOne(code);
    }

    @Override
    public boolean saveDictionaryType(DictionaryType dictionaryType) {
        return dictionaryTypeDao.insert(dictionaryType) > 0;
    }
}

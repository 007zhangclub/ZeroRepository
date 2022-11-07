package com.bjpowernode.crm.settings.service.impl;

import com.bjpowernode.crm.settings.dao.DictionaryTypeDao;
import com.bjpowernode.crm.settings.dao.DictionaryValueDao;
import com.bjpowernode.crm.settings.domain.DictionaryType;
import com.bjpowernode.crm.settings.domain.DictionaryValue;
import com.bjpowernode.crm.settings.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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

    @Autowired
    private DictionaryValueDao dictionaryValueDao;

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

    @Override
    public boolean updateDictionaryType(DictionaryType dictionaryType) {
        return dictionaryTypeDao.update(dictionaryType) > 0;
    }

    @Override
    public List<String> deleteDictionaryList(List<String> codes) {

        //封装可以删除和无法删除的List集合
        List<String> deleteList = new ArrayList<>();
        List<String> relationList = new ArrayList<>();

        //根据codes查询出关联的字典值列表数据
        codes.forEach(
                code -> {
                    List<DictionaryValue> dictionaryValueList = dictionaryValueDao.findList(code);

                    if(CollectionUtils.isEmpty(dictionaryValueList))
                        //证明当前的code,没有关联关系,可以删除
                        deleteList.add(code);
                    else
                        relationList.add(code);
                }
        );

        //判断
        if(!CollectionUtils.isEmpty(deleteList))
            //批量删除操作
            dictionaryTypeDao.deleteList(deleteList);

        return CollectionUtils.isEmpty(relationList) ? null : relationList;
    }

    @Override
    public List<DictionaryValue> findDictionaryValueList() {
        return dictionaryValueDao.findAll();
    }
}

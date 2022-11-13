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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public boolean saveDictionaryValue(DictionaryValue dictionaryValue) {
        return dictionaryValueDao.insert(dictionaryValue) > 0;
    }

    @Override
    public Map<String, List<DictionaryValue>> findCacheData() {
        //缓存数据
//        Map<String, List<DictionaryValue>> resultMap = new HashMap<>();

        //方式1,普通方式获取
        //查询出所有的字典类型编码数据,并进行遍历操作
//        dictionaryTypeDao.findAll().forEach(
//                dictionaryType -> {
//                    //获取字典类型编码,根据它来查询多方的数据列表
//                    String code = dictionaryType.getCode();
//
//                    List<DictionaryValue> dictionaryValueList = dictionaryValueDao.findList(code);
//                    System.out.println("key : "+code);
//                    System.out.println("dictionaryValueList : "+dictionaryValueList);
//                    if(!CollectionUtils.isEmpty(dictionaryValueList))
//                        resultMap.put(code,dictionaryValueList);
//                }
//        );

        //方式2(了解),通过stream api来进行集合的分组,获取对应的数据
        //我们可以通过查看的方式,能够知道DictionaryValue对象中就已经包含了我们的编码数据
        //所以我们可以通过查询所有的字典值的列表数据,然后通过stream api来处理这些数据
        //通过我们根据编码来进行分组,得到我们想要的结果
        Map<String, List<DictionaryValue>> resultMap = dictionaryValueDao.findAll().stream().collect(Collectors.groupingBy(DictionaryValue::getTypeCode));

//        System.out.println(resultMap);

        //封装
        return resultMap;
    }
}

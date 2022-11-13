package com.bjpowernode.crm.settings.service;

import com.bjpowernode.crm.settings.domain.DictionaryType;
import com.bjpowernode.crm.settings.domain.DictionaryValue;

import java.util.List;
import java.util.Map;

public interface DictionaryService {
    List<DictionaryType> findDictionaryTypeList();

    DictionaryType findDictionaryType(String code);

    boolean saveDictionaryType(DictionaryType dictionaryType);

    boolean updateDictionaryType(DictionaryType dictionaryType);

    List<String> deleteDictionaryList(List<String> codes);

    List<DictionaryValue> findDictionaryValueList();

    boolean saveDictionaryValue(DictionaryValue dictionaryValue);

    Map<String, List<DictionaryValue>> findCacheData();
}

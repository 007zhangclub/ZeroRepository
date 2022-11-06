package com.bjpowernode.crm.settings.service;

import com.bjpowernode.crm.settings.domain.DictionaryType;

import java.util.List;

public interface DictionaryService {
    List<DictionaryType> findDictionaryTypeList();

}

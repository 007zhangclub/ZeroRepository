package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.workbench.dao.ClueDao;
import com.bjpowernode.crm.workbench.dao.ClueRemarkDao;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.domain.Clue;
import com.bjpowernode.crm.workbench.domain.ClueActivityRelation;
import com.bjpowernode.crm.workbench.domain.ClueRemark;
import com.bjpowernode.crm.workbench.service.ClueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClueServiceImpl implements ClueService {
    @Autowired
    private ClueDao clueDao;

    @Autowired
    private ClueRemarkDao clueRemarkDao;

    @Override
    public boolean saveClue(Clue clue) {
        return clueDao.insert(clue) > 0;
    }

    @Override
    public Clue findClue(String id) {
        return clueDao.findById(id);
    }

    @Override
    public List<ClueRemark> findClueRemarkList(String clueId) {
        return clueRemarkDao.findList(clueId);
    }

    @Override
    public boolean saveClueRemark(ClueRemark clueRemark) {
        return clueRemarkDao.insert(clueRemark) > 0;
    }

    @Override
    public List<Activity> findClueActivityRelationList(String clueId) {
        return clueDao.findActivityRelationList(clueId);
    }

    @Override
    public boolean deleteClueActivityRelation(String carId) {
        return clueDao.deleteClueActivityRelation(carId) > 0;
    }

    @Override
    public List<Activity> findClueActivityUnRelationList(String clueId) {
        return clueDao.findClueActivityUnRelationList(clueId);
    }

    @Override
    public List<Activity> findClueActivityUnRelationList(String clueId, String activityName) {
        return clueDao.findClueActivityUnRelationListLike(clueId,activityName);
    }

    @Override
    public boolean saveClueActivityRelationList(List<ClueActivityRelation> clueActivityRelationList) {
        return clueDao.insertClueActivityRelationList(clueActivityRelationList) > 0;
    }
}

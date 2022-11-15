package com.bjpowernode.crm.workbench.service;

import com.bjpowernode.crm.workbench.domain.*;

import java.util.List;

public interface ClueService {
    boolean saveClue(Clue clue);

    Clue findClue(String id);

    List<ClueRemark> findClueRemarkList(String clueId);

    boolean saveClueRemark(ClueRemark clueRemark);

    List<Activity> findClueActivityRelationList(String clueId);

    boolean deleteClueActivityRelation(String carId);

    List<Activity> findClueActivityUnRelationList(String clueId);

    List<Activity> findClueActivityUnRelationList(String clueId, String activityName);

    boolean saveClueActivityRelationList(List<ClueActivityRelation> clueActivityRelationList);

    List<Activity> findClueActivityRelationList(String clueId, String activityName);

    void saveClueConvert(String clueId, String flag, Tran tran, String owner, String name, String time);
}

package com.bjpowernode.crm.workbench.service;

import com.bjpowernode.crm.workbench.domain.Clue;
import com.bjpowernode.crm.workbench.domain.ClueRemark;

import java.util.List;

public interface ClueService {
    boolean saveClue(Clue clue);

    Clue findClue(String id);

    List<ClueRemark> findClueRemarkList(String clueId);

    boolean saveClueRemark(ClueRemark clueRemark);
}

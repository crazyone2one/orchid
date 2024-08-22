package cn.master.backend.constants;

import lombok.Getter;

/**
 * @author Created by 11's papa on 08/19/2024
 **/
@Getter
public enum BugPlatform {

    /**
     * 本地
     */
    LOCAL("Local"),
    JIRA("JIRA"),
    ZENTAO("禅道"),
    TAPD("TAPD");


    private final String name;

    BugPlatform(String name) {
        this.name = name;
    }
}

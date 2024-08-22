package cn.master.backend.constants;

import cn.master.backend.handler.uid.ValuedEnum;

/**
 * @author Created by 11's papa on 08/20/2024
 **/
public enum WorkerNodeType implements ValuedEnum<Integer> {

    CONTAINER(1), ACTUAL(2);

    /**
     * Lock type
     */
    private final Integer type;

    /**
     * Constructor with field of type
     */
    private WorkerNodeType(Integer type) {
        this.type = type;
    }

    @Override
    public Integer value() {
        return type;
    }

}

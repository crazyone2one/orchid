package cn.master.backend.constants;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
public enum OperationLogType {
    ADD,
    DELETE,
    UPDATE,
    DEBUG,
    REVIEW,
    COPY,
    EXECUTE,
    SHARE,
    RESTORE,
    IMPORT,
    EXPORT,
    LOGIN,
    SELECT,
    RECOVER,
    LOGOUT,
    DISASSOCIATE,
    ASSOCIATE,
    QRCODE,
    ARCHIVED,
    STOP;

    public boolean contains(OperationLogType keyword) {
        return this.name().contains(keyword.name());
    }
}

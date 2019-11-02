package org.easysql.info;

public interface CRUD_VALUE {
    int ONLY_VALUE=31;
    int ALL_VALUE=37;

    int DESC=7;
    int ASC=5;
    int NUMBER_SORT=9;
    int LONG_NUMBER_SORT=17;
    int STRING_SORT=11;

    int READ_MODE=67;
    int READ_WRITE_MODE=13;

    int ORIGIN_DATA_INDEX=0;
    int INSERTED_DATA_INDEX=1;
    int UPDATED_DATA_INDEX=2;
    int DELETED_DATA_INDEX=3;
}

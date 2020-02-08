package org.easysql.helper;

public interface CommonValue {
    String PROCESS = "process: ";
    String SUGGESTION = "suggestion: ";
    String SQL_OUTPUT = "sql output: ";
    String DATA_OUTPUT="data output: ";
    String ERROR = "error: ";

    String ADD_COLUMN = "add column:";
    String DELETE_COLUMN = "delete column:";
    String ALTER_COLUMN_NAME = "alter column name:";
    String ALTER_COLUMN_TYPE = "alter column type:";
    String ALTER_TABLE_NAME = "alter table name:";

    int ONLY_VALUE = 31;
    int ALL_VALUE = 37;

    int DESC = 7;
    int ASC = 5;
    int NUMBER_SORT = 9;
    int LONG_NUMBER_SORT = 17;
    int STRING_SORT = 11;

    int READ_MODE = 67;
    int READ_WRITE_MODE = 13;

    int ORIGIN_DATA_INDEX = 0;
    int INSERTED_DATA_INDEX = 1;
    int UPDATED_DATA_INDEX = 2;
    int DELETED_DATA_INDEX = 3;
}

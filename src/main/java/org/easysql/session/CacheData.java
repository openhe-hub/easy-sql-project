package org.easysql.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
@AllArgsConstructor
public class CacheData<T> {
    private T data;
    private int type;
}


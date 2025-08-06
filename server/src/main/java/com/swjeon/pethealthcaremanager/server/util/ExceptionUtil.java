package com.swjeon.pethealthcaremanager.server.util;

import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;

public class ExceptionUtil {
    public static final int UNKNOWN_EXCEPTION = 0;
    public static final int FOREIGN_KEY_EXCEPTION = 1;
    public static final int UNIQUE_KEY_EXCEPTION = 2;
    public static int getErrorCode(Throwable e) {
        if (e instanceof DataIntegrityViolationException) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            if (root instanceof SQLIntegrityConstraintViolationException ex) {
                switch (ex.getErrorCode()) {
                    case 1451: //삭제 중 외래키 위반
                    case 1452: //삽입 중 외래키 위반
                        return FOREIGN_KEY_EXCEPTION;
                    case 1062: //삽입/갱신 중 주요 키 위반
                        return UNIQUE_KEY_EXCEPTION;
                    default: //그외
                        return UNKNOWN_EXCEPTION;
                }
            }
        }
        return UNKNOWN_EXCEPTION;
    }
}

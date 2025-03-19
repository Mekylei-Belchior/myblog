package br.com.mekylei.myblog.exceptions;

import br.com.mekylei.myblog.enums.NotFoundBy;

public class NewsNotFoundByException extends RuntimeException {

    public NewsNotFoundByException(NotFoundBy by, String value) {
        super(NotFoundBy.TAG.equals(by) ? "News not found by tag name: " + value : "News not found by title: " + value);
    }

}

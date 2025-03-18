package br.com.mekylei.myblog.exceptions;

public class NewsNotFoundException extends RuntimeException {

    public NewsNotFoundException(String message) {
        super(message);
    }

}

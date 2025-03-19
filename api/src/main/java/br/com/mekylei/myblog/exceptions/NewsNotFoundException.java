package br.com.mekylei.myblog.exceptions;

public class NewsNotFoundException extends RuntimeException {

    public NewsNotFoundException(long idNews) {
        super("News not found for id: " + idNews);
    }

    public NewsNotFoundException(String message) {
        super(message);
    }

}

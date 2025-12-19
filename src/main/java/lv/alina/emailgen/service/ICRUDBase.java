package lv.alina.emailgen.service;

import java.util.ArrayList;

public interface ICRUDBase <T> {
	
    public abstract ArrayList<T> retrieveAll() throws Exception;

    public abstract T retrieveById(Long id) throws Exception;

    public abstract void deleteById(Long id) throws Exception;

}

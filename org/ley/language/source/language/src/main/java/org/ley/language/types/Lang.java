package org.ley.language.types;


import lombok.Data;

@Data
public class Lang {

    private String path;
    private String id;
    private String name;

    public static Lang get(String path) {
        return null;
    }

}

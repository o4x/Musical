package com.o4x.musical.ui.fragments.bugreport.model.github;

public class GithubTarget {
    private final String username;

    private final String repository;

    public GithubTarget(String username, String repository) {
        this.username = username;
        this.repository = repository;
    }

    public String getUsername() {
        return username;
    }

    public String getRepository() {
        return repository;
    }
}

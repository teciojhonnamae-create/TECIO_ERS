package com.eventreservation.ui;

import com.eventreservation.dao.UserDao;

import java.util.Scanner;

public class Menu {

    private final Authentication authentication;

    public Menu(
            UserDao userDao,
            Scanner scanner
    ) {
        this.authentication = new Authentication(
                userDao,
                scanner
        );
    }

    /**
     * Starts the authentication menu.
     */
    public void start() {
        authentication.start();
    }
}